package xyz.gnarbot.gnar.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import io.sentry.Sentry
import org.redisson.api.RQueue
import xyz.gnarbot.gnar.Bot
import xyz.gnarbot.gnar.commands.music.embedTitle
import xyz.gnarbot.gnar.commands.music.embedUri
import xyz.gnarbot.gnar.utils.PlaylistUtils
import xyz.gnarbot.gnar.utils.response.respond
import java.util.*

class TrackScheduler(private val bot: Bot, private val manager: MusicManager, private val player: AudioPlayer) : AudioEventAdapter() {
    //Base64 encoded.
    val queue: RQueue<String> = bot.db().redisson.getQueue("playerQueue:${manager.guildId}")
    var repeatOption = RepeatOption.NONE
    var lastTrack: AudioTrack? = null
        private set

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            queue.offer(PlaylistUtils.toBase64String(track))
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    fun nextTrack() {
        if (queue.isEmpty()) {
            manager.discordFMTrack?.let {
                it.nextDiscordFMTrack(manager)

                //This basically forces it to poll the next track immediately, they skipped it.
                val track = queue.poll()
                player.startTrack(PlaylistUtils.toAudioTrack(track), false)

                return
            }

            manager.playerRegistry.executor.execute { manager.playerRegistry.destroy(manager.getGuild()) }
            return
        }

        val track = queue.poll()
        val decodedTrack = PlaylistUtils.toAudioTrack(track)
        player.startTrack(decodedTrack, false)

        // fixme DI point
        if (bot.options.ofGuild(manager.getGuild()).music.announce) {
            announceNext(decodedTrack)
        }
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        this.lastTrack = track

        if (endReason.mayStartNext) {
            when (repeatOption) {
                RepeatOption.SONG -> {
                    val newTrack = track.makeClone().also { it.userData = track.userData }
                    player.startTrack(newTrack, false)
                }
                RepeatOption.QUEUE -> {
                    val newTrack = track.makeClone().also { it.userData = track.userData }
                    queue.offer(PlaylistUtils.toBase64String(newTrack))
                    nextTrack()
                }
                RepeatOption.NONE -> nextTrack()
            }
        }
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long, stackTrace: Array<out StackTraceElement>) {
        track.getUserData(TrackContext::class.java).requestedChannel.let {
            manager.getGuild()?.getTextChannelById(it)
        }?.respond()?.error(
                "The track ${track.info.embedTitle} is stuck longer than ${thresholdMs}ms threshold."
        )?.queue()

        val exc = buildString {
            append("AudioTrack (${track.info.identifier}) stuck >=${thresholdMs}ms\n")
            for (line in stackTrace) {
                val trace = line.toString().split('/').last() // java.base@<version>/<info>
                append(trace)
                append("\n")
            }
        }

        print(exc)
        Sentry.capture(exc)
        nextTrack()
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) {
        if (exception.toString().contains("decoding")) {
            return
        }
        track.getUserData(TrackContext::class.java).requestedChannel.let {
            manager.getGuild()?.getTextChannelById(it)
        }?.respond()?.exception(exception)?.queue()

        Sentry.capture(exception)
    }

    private fun announceNext(track: AudioTrack) {
        manager.currentRequestChannel?.let {
            it.respond().embed("Music Playback") {
                desc {
                    buildString {
                        append("Now playing __**[").append(track.info.embedTitle)
                        append("](").append(track.info.embedUri).append(")**__")

                        track.getUserData(TrackContext::class.java)?.requester?.let { manager.getGuild()?.getMemberById(it) }?.let {
                            append(" requested by ")
                            append(it.asMention)
                        }

                        append(".")
                    }
                }
            }.action().queue()
        }
    }

    fun shuffle() = (queue as MutableList<*>).shuffle()

    fun removeQueueIndex(queue: Queue<String>, indexToRemove: Int) : String {
        var index = 0
        val iterator = queue.iterator()
        while (iterator.hasNext() && index <= indexToRemove) {
            iterator.next()
            if (index == indexToRemove) {
                iterator.remove()
            }
            index++
        }
    }
}
