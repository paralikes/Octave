package xyz.gnarbot.gnar.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import xyz.gnarbot.gnar.Launcher

class DiscordFMTrackContext(
        val station: String,
        requester: Long,
        requestedChannel: Long
) : TrackContext(requester, requestedChannel) {
    companion object {
        @JvmStatic
        val errorTolerance = 3
    }

    fun nextDiscordFMTrack(musicManager: MusicManager, errorDepth: Int = 0) {
        if (errorDepth > errorTolerance) {
            return Launcher.players.destroy(musicManager.guild)
        }

        val randomSong = Launcher.discordFm.getRandomSong(station)
            ?: return nextDiscordFMTrack(musicManager, errorDepth + 1)

        musicManager.playerManager.loadItemOrdered(this, randomSong, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                track.userData = this@DiscordFMTrackContext
                musicManager.scheduler.queue(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) = trackLoaded(playlist.tracks.first())

            override fun noMatches() = Launcher.players.destroy(musicManager.guild)

            override fun loadFailed(exception: FriendlyException) {
                if (errorDepth >= errorTolerance) {
                    Launcher.players.destroy(musicManager.guild)
                } else {
                    nextDiscordFMTrack(musicManager, errorDepth + 1)
                }
            }
        })
    }
}