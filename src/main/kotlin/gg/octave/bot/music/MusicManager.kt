package gg.octave.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gg.octave.bot.Launcher
import gg.octave.bot.commands.music.embedTitle
import gg.octave.bot.commands.music.embedUri
import gg.octave.bot.music.filters.DSPFilter
import gg.octave.bot.music.sources.caching.CachingSourceManager
import gg.octave.bot.utils.extensions.data
import gg.octave.bot.utils.extensions.friendlierMessage
import gg.octave.bot.utils.extensions.premiumGuild
import gg.octave.bot.utils.extensions.voiceChannel
import gg.octave.bot.utils.getDisplayValue
import me.devoxin.flight.api.Context
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.internal.audio.AudioConnection
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class MusicManager(val bot: Launcher, val guildId: String, val playerRegistry: PlayerRegistry, val playerManager: AudioPlayerManager) {
    fun search(query: String, maxResults: Int = -1, callback: (results: List<AudioTrack>) -> Unit) {
        playerManager.loadItem(query, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) = callback(listOf(track))

            override fun playlistLoaded(playlist: AudioPlaylist) {
                if (!playlist.isSearchResult) {
                    return
                }

                if (maxResults == -1) {
                    callback(playlist.tracks)
                } else {
                    callback(playlist.tracks.subList(0, maxResults.coerceAtMost(playlist.tracks.size)))
                }
            }

            override fun noMatches() = callback(emptyList())
            override fun loadFailed(e: FriendlyException) = callback(emptyList())
        })
    }

    @Volatile
    private var leaveTask: Future<*>? = null

    /** @return Audio player for the guild. */
    val player = playerManager.createPlayer()
    val dspFilter = DSPFilter(player)

    /**  @return Track scheduler for the player.*/
    val scheduler = TrackScheduler(this, player).also(player::addListener)

    /** @return Wrapper around AudioPlayer to use it as an AudioSendHandler. */
    private val sendHandler: AudioPlayerSendHandler = AudioPlayerSendHandler(player)

    private val dbAnnouncementChannel: String?
        get() = bot.db.getGuildData(guildId)?.music?.announcementChannel

    /**
     * @return Voting cooldown.
     */
    var lastVoteTime: Long = 0L

    /**
     * @return Whether there is a vote to skip the song or not.
     */
    var isVotingToSkip = false
    var isVotingToPlay = false
    var lastPlayVoteTime: Long = 0L

    val currentRequestChannel: TextChannel?
        get() {
            return (player.playingTrack ?: scheduler.lastTrack)
                ?.getUserData(TrackContext::class.java)
                ?.requestedChannel
                ?.let { it -> guild?.getTextChannelById(it) }
        }

    val announcementChannel: TextChannel?
        get() {
            val dbAnnChn = dbAnnouncementChannel
            return when {
                dbAnnChn != null -> guild!!.getTextChannelById(dbAnnChn)
                else -> currentRequestChannel
            }
        }

    val guild: Guild?
        get() = Launcher.shardManager.getGuildById(guildId)

    var loops = 0L

    /**
     * @return If the user is listening to DiscordFM
     */
    var discordFMTrack: DiscordFMTrackContext? = null

    init {
        if (bot.configuration.setActivityToSong) {
            player.addListener(ActivityUpdater(bot, guildId))
        }
    }

    fun destroy() {
        scheduler.queue.expire(4, TimeUnit.HOURS)
        player.destroy()
        dspFilter.clearFilters()
        closeAudioConnection()
    }

    fun openAudioConnection(channel: VoiceChannel, ctx: Context): Boolean {
        when {
            !guild?.selfMember!!.hasPermission(channel, Permission.VOICE_CONNECT) -> {
                ctx.send("The bot can't connect to this channel due to a lack of permission.")
                playerRegistry.destroy(guild)
                return false
            }
            channel.userLimit != 0
                && guild?.selfMember!!.hasPermission(channel, Permission.VOICE_MOVE_OTHERS)
                && channel.members.size >= channel.userLimit -> {
                ctx.send("The bot can't join due to the user limit.")
                playerRegistry.destroy(guild)
                return false
            }
            else -> {
                guild?.audioManager?.apply {
                    openAudioConnection(channel)
                    sendingHandler = sendHandler
                }

                ctx.send {
                    setTitle("Music Playback")
                    setDescription("Joining channel `${channel.name}`.")
                }
                return true
            }
        }
    }

    fun moveAudioConnection(channel: VoiceChannel) {
        guild?.let {
            if (!it.selfMember.voiceState!!.inVoiceChannel()) {
                throw IllegalStateException("Bot is not in a voice channel")
            }

            if (!it.selfMember.hasPermission(channel, Permission.VOICE_CONNECT)) {
                currentRequestChannel?.sendMessage("I don't have permission to join `${channel.name}`.")?.queue()
                playerRegistry.destroy(it)
                return
            }

            player.isPaused = true
            it.audioManager.openAudioConnection(channel)
            player.isPaused = false

            currentRequestChannel?.sendMessage(EmbedBuilder().apply {
                setTitle("Music Playback")
                setDescription("Moving to channel `${channel.name}`.")
            }.build())?.queue()
        }
    }

    fun closeAudioConnection() {
        guild?.audioManager?.apply {
            closeAudioConnection()
            sendingHandler = null
        }
    }

    fun isAlone() = guild?.selfMember?.voiceState?.channel?.members?.none { !it.user.isBot } ?: true

    val leaveQueued: Boolean
        get() = leaveTask != null

    fun queueLeave() {
        leaveTask?.cancel(false)
        leaveTask = createLeaveTask()
        player.isPaused = true
    }

    fun cancelLeave() {
        leaveTask?.cancel(false)
        leaveTask = null
        player.isPaused = false
    }

    private fun createLeaveTask() = schedulerThread.schedule({ playerRegistry.destroy(guild) }, 30, TimeUnit.SECONDS)

    fun loadAndPlay(ctx: Context, trackUrl: String, trackContext: TrackContext, footnote: String? = null, isNext: Boolean, shuffle: Boolean = false, resultHandler: AudioLoadResultHandler? = null) {
        playerManager.loadItemOrdered(this, trackUrl, MusicManagerAudioLoadResultHandler(ctx, trackUrl, trackContext, footnote, isNext, shuffle, resultHandler))
    }

    inner class MusicManagerAudioLoadResultHandler(val ctx: Context, val trackUrl: String, val trackContext: TrackContext, val footnote: String? = null, val isNext: Boolean, val shuffle: Boolean = false, val resultHandler: AudioLoadResultHandler? = null) : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            cache(trackUrl, track)

            if (!guild?.selfMember!!.voiceState!!.inVoiceChannel()) { // wtf is this mess
                if (!openAudioConnection(ctx.voiceChannel!!, ctx)) {
                    return
                }
            }

            val queueLimit = queueLimit(ctx)
            val queueLimitDisplay = when (queueLimit) {
                Integer.MAX_VALUE -> "unlimited"
                else -> queueLimit.toString()
            }

            if (scheduler.queue.size >= queueLimit) {
                return ctx.send("The queue can not exceed $queueLimitDisplay songs.")
            }

            if (!track.info.isStream) {
                val data = ctx.data
                val premiumGuild = ctx.premiumGuild
                val invalidDuration = premiumGuild == null && data.music.maxSongLength > bot.configuration.durationLimit.toMillis()

                val durationLimit = when {
                    data.music.maxSongLength != 0L && !invalidDuration -> data.music.maxSongLength
                    premiumGuild != null -> premiumGuild.songLengthQuota
                    data.isPremium -> TimeUnit.MINUTES.toMillis(360) //Keep key perks.
                    else -> bot.configuration.durationLimit.toMillis()
                }

                val durationLimitText = when {
                    data.music.maxSongLength != 0L && !invalidDuration -> getDisplayValue(data.music.maxSongLength)
                    premiumGuild != null -> getDisplayValue(premiumGuild.songLengthQuota)
                    data.isPremium -> getDisplayValue(TimeUnit.MINUTES.toMillis(360)) //Keep key perks.
                    else -> bot.configuration.durationLimitText
                }

                if (track.duration > durationLimit) {
                    return ctx.send("The track can not exceed $durationLimitText.")
                }
            }

            track.userData = trackContext
            scheduler.queue(track, isNext)

            ctx.send {
                setTitle("Music Queue")
                setDescription("Added __**[${track.info.embedTitle}](${track.info.embedUri})**__ to queue.")
                setFooter(footnote)
            }

            resultHandler?.trackLoaded(track)
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            cache(trackUrl, playlist)

            if (playlist.isSearchResult) {
                return trackLoaded(playlist.tracks.first())
            }

            val queueLimit = queueLimit(ctx)
            val queueLimitDisplay = when (queueLimit) {
                Integer.MAX_VALUE -> "unlimited"
                else -> queueLimit.toString()
            }

            if (!guild?.selfMember!!.voiceState!!.inVoiceChannel()) {
                if (!ctx.member!!.voiceState!!.inVoiceChannel()) {
                    ctx.send("You left the channel before the track is loaded.")

                    // Track is not supposed to load and the queue is empty
                    // destroy player
                    if (scheduler.queue.isEmpty()) {
                        playerRegistry.destroy(guild)
                    }
                    return
                }
                if (!openAudioConnection(ctx.voiceChannel!!, ctx)) {
                    return
                }
            }

            val tracks = playlist.tracks
            var ignored = 0

            var added = 0
            for (track in if (shuffle) tracks.shuffled() else tracks) {
                if (scheduler.queue.size + 1 >= queueLimit) {
                    ignored = tracks.size - added
                    break
                }

                track.userData = trackContext

                scheduler.queue(track, isNext)
                added++
            }

            ctx.send {
                setTitle("Music Queue")
                val desc = buildString {
                    append("Added `$added` tracks to queue from playlist `${playlist.name}`.\n")
                    if (ignored > 0) {
                        append("Ignored `$ignored` songs as the queue can not exceed `$queueLimitDisplay` songs.")
                    }
                }
                setDescription(desc)
                setFooter(footnote)
            }

            resultHandler?.playlistLoaded(playlist)
        }

        override fun noMatches() {
            // No track found and queue is empty
            // destroy player
            if (player.playingTrack == null && scheduler.queue.isEmpty()) {
                playerRegistry.destroy(guild)
            }

            ctx.send("Nothing found by `$trackUrl`")

            resultHandler?.noMatches()
        }

        override fun loadFailed(e: FriendlyException) {
            // No track found and queue is empty
            // destroy player

            if (e.message!!.contains("decoding")) {
                return
            }

            if (player.playingTrack == null && scheduler.queue.isEmpty()) {
                playerRegistry.destroy(guild)
            }

            ctx.send(e.friendlierMessage())

            resultHandler?.loadFailed(e)
        }
    }

    fun queueLimit(ctx: Context): Int {
        val premiumGuild = ctx.premiumGuild
        val data = ctx.data
        val invalidSize = premiumGuild == null && data.music.maxQueueSize > bot.configuration.queueLimit

        return when {
            data.music.maxQueueSize != 0 && !invalidSize -> data.music.maxQueueSize
            premiumGuild != null -> premiumGuild.queueSizeQuota
            data.isPremium -> 500 //Keep key perks.
            else -> bot.configuration.queueLimit
        }
    }

    companion object {
        val schedulerThread = Executors.newSingleThreadScheduledExecutor()
        fun cache(identifier: String, item: AudioItem) = CachingSourceManager.cache(identifier, item)
    }
}
