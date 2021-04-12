package gg.octave.bot.music.sources.spotify.loaders

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioItem
import gg.octave.bot.music.sources.spotify.SpotifyAudioSourceManager
import org.apache.http.HttpStatus
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import java.util.regex.Matcher

class SpotifyTrackLoader : Loader {

    override fun pattern() = TRACK_PATTERN

    override fun load(manager: AudioPlayerManager, sourceManager: SpotifyAudioSourceManager, matcher: Matcher): AudioItem? {
        val trackId = matcher.group(2)
        val spotifyTrack = fetchTrackInfo(sourceManager, trackId)
        val trackArtists = spotifyTrack.getJSONArray("artists")
        val trackArtist = if (trackArtists.isEmpty) "" else trackArtists.getJSONObject(0).getString("name")
        val trackTitle = spotifyTrack.getString("name")

        return sourceManager.doYoutubeSearch(manager, "ytsearch:$trackArtist $trackTitle")
    }

    private fun fetchTrackInfo(sourceManager: SpotifyAudioSourceManager, trackId: String): JSONObject {
        return sourceManager.request("https://api.spotify.com/v1/tracks/$trackId") {
            addHeader("Authorization", "Bearer ${sourceManager.accessToken}")
        }.use {
            check(it.statusLine.statusCode == HttpStatus.SC_OK) {
                "Received code ${it.statusLine.statusCode} from Spotify while fetching track information"
            }

            val content = EntityUtils.toString(it.entity)
            JSONObject(content)
        }
    }

    companion object {
        //private val PLAYLIST_PATTERN = "^https?://(?:open\\.)?spotify\\.com/track/([a-zA-Z0-9]+)".toPattern()
        private val TRACK_PATTERN = "^(?:https?://(?:open\\.)?spotify\\.com|spotify)([/:])track\\1([a-zA-Z0-9]+)".toPattern()
    }

}
