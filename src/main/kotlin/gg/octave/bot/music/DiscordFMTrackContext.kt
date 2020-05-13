package gg.octave.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import gg.octave.bot.Launcher
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.concurrent.CompletableFuture

class DiscordFMTrackContext(
    val station: String,
    requester: Long,
    requestedChannel: Long
) : TrackContext(requester, requestedChannel) {
    override fun serialize(stream: ByteArrayOutputStream) {
        fun serialize(stream: ByteArrayOutputStream) {
            val writer = DataOutputStream(stream)
            writer.writeInt(2)
            // 1 => TrackContext
            // 2 => DiscordFMTrackContext
            writer.writeLong(requester)
            writer.writeLong(requestedChannel)
            writer.writeUTF(station)
            writer.close() // This invokes flush.
        }
    }

    fun nextDiscordFMTrack(musicManager: MusicManager, errorDepth: Int = 0): CompletableFuture<AudioTrack?> {
        if (errorDepth > errorTolerance) {
            return CompletableFuture.completedFuture(null)
        }

        val randomSong = Launcher.discordFm.getRandomSong(station)
            ?: return nextDiscordFMTrack(musicManager, errorDepth + 1)

        val future = CompletableFuture<AudioTrack?>()

        musicManager.playerManager.loadItemOrdered(this, randomSong, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                track.userData = this@DiscordFMTrackContext
                future.complete(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) = trackLoaded(playlist.tracks.first())

            override fun noMatches() {
                future.complete(null)
            }

            override fun loadFailed(exception: FriendlyException) {
                if (errorDepth >= errorTolerance) {
                    future.complete(null)
                } else {
                    nextDiscordFMTrack(musicManager, errorDepth + 1)
                }
            }
        })

        return future
    }

    companion object {
        @JvmStatic
        val errorTolerance = 3
    }
}
