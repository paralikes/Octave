package gg.octave.bot.music.sources.spotify.loaders

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import gg.octave.bot.music.sources.spotify.SpotifyAudioSourceManager
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CompletableFuture
import java.util.regex.Matcher

class SpotifyAlbumLoader : Loader {

    override fun pattern() = PLAYLIST_PATTERN

    override fun load(manager: AudioPlayerManager, sourceManager: SpotifyAudioSourceManager, matcher: Matcher): AudioItem {
        val albumId = matcher.group(2)
        val albumInfo = fetchAlbumInfo(sourceManager, albumId)

        val trackObj = albumInfo.getJSONObject("tracks")
        check(trackObj.has("items")) { "Album $albumId is missing track items!" }
        val trackList = trackObj.getJSONArray("items")
        check(!trackList.isEmpty) { "Album $albumId track list is empty!" }

        val albumTracks = fetchAlbumTracks(manager, sourceManager, trackList)
        val albumName = albumInfo.optString("name")

        return BasicAudioPlaylist(albumName, albumTracks, null, false)
    }

    private fun fetchAlbumInfo(sourceManager: SpotifyAudioSourceManager, albumId: String): JSONObject {
        return sourceManager.request("https://api.spotify.com/v1/albums/$albumId") {
            addHeader("Authorization", "Bearer ${sourceManager.accessToken}")
        }.use {
            check(it.statusLine.statusCode == HttpStatus.SC_OK) {
                "Received code ${it.statusLine.statusCode} from Spotify while fetching album tracks"
            }

            val content = EntityUtils.toString(it.entity)
            JSONObject(content)
        }
    }

    private fun fetchAlbumTracks(manager: AudioPlayerManager,
                                 sourceManager: SpotifyAudioSourceManager, jsonTracks: JSONArray): List<AudioTrack> {
        val tasks = mutableListOf<CompletableFuture<AudioTrack>>()

        for (jTrack in jsonTracks) {
            val track = (jTrack as JSONObject)
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

        return tasks.filterNot { t -> t.isCompletedExceptionally }
            .mapNotNull { t -> t.get() }
    }

    companion object {
        private val PLAYLIST_PATTERN = "^(?:https?://(?:open\\.)?spotify\\.com|spotify)([/:])album\\1([a-zA-Z0-9]+)".toPattern()
    }

}
