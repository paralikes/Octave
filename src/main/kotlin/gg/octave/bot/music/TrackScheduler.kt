package gg.octave.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import gg.octave.bot.Launcher
import gg.octave.bot.commands.music.embedTitle
import gg.octave.bot.commands.music.embedUri
import gg.octave.bot.db.OptionsRegistry
import gg.octave.bot.music.settings.RepeatOption
import gg.octave.bot.utils.PlaylistUtils
import gg.octave.bot.utils.extensions.friendlierMessage
import gg.octave.bot.utils.extensions.insertAt
import io.sentry.Sentry
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.StackTraceInterface
import net.dv8tion.jda.api.EmbedBuilder
import org.redisson.api.RQueue
import java.util.*

class TrackScheduler(private val manager: MusicManager, private val player: AudioPlayer) : AudioEventAdapter() {
    //Base64 encoded.
    val queue: RQueue<String> = Launcher.db.redisson.getQueue("playerQueue:${manager.guildId}")
    var repeatOption = RepeatOption.NONE
    var lastTrack: AudioTrack? = null
        private set
    var currentTrack: AudioTrack? = null
        private set

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    fun queue(track: AudioTrack, isNext: Boolean) {
        if (!player.startTrack(track, true)) {
            if (isNext) {
                insertAt(0, track)
            } else {
                queue.offer(PlaylistUtils.toBase64String(track))
            }
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    fun nextTrack() {
        if (repeatOption != RepeatOption.NONE) {
            val cloneThis = currentTrack
                ?: return

            val cloned = cloneThis.makeClone().also { it.userData = cloneThis.userData }
            // Pretty sure makeClone now copies user data, but better to be safe than sorry.

            if (repeatOption == RepeatOption.SONG) {
                return player.playTrack(cloned)
            } else if (repeatOption == RepeatOption.QUEUE) {
                queue.offer(PlaylistUtils.toBase64String(cloned))
            } // NONE doesn't need any handling.
        }

        if (queue.isNotEmpty()) {
            val track = queue.poll()
            val decodedTrack = PlaylistUtils.toAudioTrack(track)
            return player.playTrack(decodedTrack)
        }

        if (manager.discordFMTrack == null) {
            return manager.playerRegistry.executor.execute { manager.playerRegistry.destroy(manager.guild) }
        }

        manager.discordFMTrack?.let {
            it.nextDiscordFMTrack(manager).thenAccept { track ->
                if (track == null) {
                    return@thenAccept Launcher.players.destroy(manager.guild)
                }

                player.startTrack(track, false)
            }
        }
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        this.lastTrack = track

        if (endReason.mayStartNext) {
            nextTrack()
        }
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long, stackTrace: Array<out StackTraceElement>) {
        val guild = manager.guild ?: return
        track.getUserData(TrackContext::class.java)
            ?.requestedChannel
            ?.let(guild::getTextChannelById)
            ?.sendMessage("The track ${track.info.embedTitle} is stuck longer than ${thresholdMs}ms threshold.")
            ?.queue()

        val eventBuilder = EventBuilder().withMessage("AudioTrack stuck longer than ${thresholdMs}ms")
            .withLevel(Event.Level.ERROR)
            .withSentryInterface(StackTraceInterface(stackTrace))

        Sentry.capture(eventBuilder)
        nextTrack()
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        repeatOption = RepeatOption.NONE

        if (exception.toString().contains("decoding")) {
            return
        }

        Sentry.capture(exception)
        val channel = track.getUserData(TrackContext::class.java)?.requestedChannel?.let {
            manager.guild?.getTextChannelById(it)
        } ?: return

        channel.sendMessage(
            "An unknown error occurred while playing **${track.info.title}**:\n${exception.friendlierMessage()}"
        ).queue()
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        currentTrack = track

        if (OptionsRegistry.ofGuild(manager.guildId).music.announce) {
            announceNext(track)
        }
    }

    private fun announceNext(track: AudioTrack) {
        val channel = manager.announcementChannel ?: return
        val description = buildString {
            append("Now playing __**[").append(track.info.embedTitle)
            append("](").append(track.info.embedUri).append(")**__")

            val reqData = track.getUserData(TrackContext::class.java)
            append(" requested by ")
            append(reqData?.requesterMention ?: "Unknown")
            append(".")
        }

        channel.sendMessage(EmbedBuilder().apply {
            setDescription(description)
        }.build()).queue()
    }

    fun shuffle() = (queue as MutableList<*>).shuffle()

    fun insertAt(index: Int, element: AudioTrack) = queue.insertAt(index, PlaylistUtils.toBase64String(element))

    companion object {
        fun getQueueForGuild(guildId: String): RQueue<String> {
            return Launcher.db.redisson.getQueue("playerQueue:$guildId")
        }
    }
}
