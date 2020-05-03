package gg.octave.bot.music.sources.spotify.loaders

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import gg.octave.bot.music.sources.spotify.SpotifyAudioSourceManager
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import java.util.concurrent.CompletableFuture
import java.util.regex.Matcher

class SpotifyPlaylistLoader : Loader {

    override fun pattern() = PLAYLIST_PATTERN

    override fun load(manager: DefaultAudioPlayerManager, sourceManager: SpotifyAudioSourceManager, matcher: Matcher): AudioItem {
        val playlistId = matcher.group(2)
        val playlistInfo = fetchPlaylistInfo(sourceManager, playlistId)
        val playlistTracks = fetchPlaylistTracks(manager, sourceManager, playlistId)
        val playlistName = playlistInfo.optString("name")

        return BasicAudioPlaylist(playlistName, playlistTracks, null, false)
    }

    private fun fetchPlaylistInfo(sourceManager: SpotifyAudioSourceManager, playlistId: String): JSONObject {
        return sourceManager.request("https://api.spotify.com/v1/playlists/$playlistId") {
            addHeader("Authorization", "Bearer ${sourceManager.accessToken}")
        }.use {
            check(it.statusLine.statusCode == HttpStatus.SC_OK) {
                "Received code ${it.statusLine.statusCode} from Spotify while fetching playlist information"
            }

            val content = EntityUtils.toString(it.entity)
            JSONObject(content)
        }
    }

    private fun fetchPlaylistTracks(manager: DefaultAudioPlayerManager,
                                    sourceManager: SpotifyAudioSourceManager, playlistId: String): List<AudioTrack> {
        return sourceManager.request("https://api.spotify.com/v1/playlists/$playlistId/tracks") {
            addHeader("Authorization", "Bearer ${sourceManager.accessToken}")
        }.use {
            check(it.statusLine.statusCode == HttpStatus.SC_OK) {
                "Received code ${it.statusLine.statusCode} from Spotify while fetching playlist tracks"
            }

            val content = EntityUtils.toString(it.entity)
            val json = JSONObject(content)

            if (!json.has("items")) {
                return emptyList()
            }

            val jsonTracks = json.getJSONArray("items")
            val tasks = mutableListOf<CompletableFuture<AudioTrack>>()

            for (jTrack in jsonTracks) {
                val trackJ = jTrack as JSONObject

                if (trackJ.isNull("track")) {
                    continue
                }

                val track = trackJ.getJSONObject("track")
                val title = track.getString("name")
                val artist = track.getJSONArray("artists").getJSONObject(0).getString("name")

                val task = sourceManager.queueYoutubeSearch(manager, "ytsearch:$title $artist")
                    .thenApply { ai -> if (ai is AudioPlaylist) ai.tracks.first() else ai as AudioTrack }
                tasks.add(task)
            }

            try {
                CompletableFuture.allOf(*tasks.toTypedArray()).get()
            } catch (ignored: Exception) {
            }

            tasks.filterNot { t -> t.isCompletedExceptionally }
                .mapNotNull { t -> t.get() }
        }
    }

    companion object {
        //        private val PLAYLIST_PATTERN = "^https?://(?:open\\.)?spotify\\.com/(?:user/[a-zA-Z0-9_]+/)?playlist/([a-zA-Z0-9]+)".toPattern()
        private const val URL_PATTERN = "https?://(?:open\\.)?spotify\\.com(?:/user/[a-zA-Z0-9_]+)?"
        private val PLAYLIST_PATTERN = "^(?:$URL_PATTERN|spotify)([/:])playlist\\1([a-zA-Z0-9]+)".toPattern()
    }

}
