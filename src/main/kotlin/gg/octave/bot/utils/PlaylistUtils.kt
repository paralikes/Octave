package gg.octave.bot.utils

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import gg.octave.bot.Launcher.players
import org.json.JSONArray
import org.json.JSONObject

object PlaylistUtils {
    private val playerManager = players.playerManager

    // Playlists
    fun decodePlaylist(encodedTracks: List<String>, name: String): BasicAudioPlaylist {
        val decoded = encodedTracks.mapNotNull(::decodeMaybeNullAudioTrack)
        return BasicAudioPlaylist(name, decoded, decoded[0], false)
    }

    fun decodePlaylist(jsonString: String): BasicAudioPlaylist {
        val jo = JSONObject(jsonString)

        val name = jo.getString("name")
        val isSearch = jo.getBoolean("search")
        val selectedIndex = jo.getInt("selected")

        val encodedTracks = jo.getJSONArray("tracks")
        val tracks = mutableListOf<AudioTrack>()

        for (encodedTrack in encodedTracks) {
            val decodedTrack = decodeAudioTrack(encodedTrack as String) ?: continue
            tracks.add(decodedTrack)
        }

        val selectedTrack = if (selectedIndex > -1) tracks[selectedIndex] else null
        return BasicAudioPlaylist(name, tracks, selectedTrack, isSearch)
    }

    fun toJsonString(playlist: AudioPlaylist): String {
        val selectedIndex = playlist.selectedTrack?.let(playlist.tracks::indexOf) ?: -1
        val tracks = JSONArray()

        for (track in playlist.tracks) {
            val enc = encodeAudioTrack(track) ?: continue
            tracks.put(enc)
        }

        return JSONObject().apply {
            put("name", playlist.name)
            put("tracks", tracks)
            put("search", playlist.isSearchResult)
            put("selected", selectedIndex)
        }.toString()
    }

    fun encodePlaylist(playlist: BasicAudioPlaylist) = encodePlaylist(playlist.tracks)
    fun encodePlaylist(playlist: Collection<AudioTrack>): List<String> = playlist.map(::encodeAudioTrack)

    fun decodeAudioTrack(encoded: String) = playerManager.decodeTrack(encoded)!!

    // This is used at the top of the file. Don't ask :^)
    fun decodeMaybeNullAudioTrack(encoded: String) = playerManager.decodeTrack(encoded)
    fun encodeAudioTrack(track: AudioTrack) = playerManager.encodeTrack(track)

    // Misc
    private fun <T : Throwable, R> suppress(exception: Class<out T>, block: () -> R): R? {
        return try {
            block()
        } catch (e: Throwable) {
            if (!exception.isAssignableFrom(e::class.java)) {
                throw e
            }

            null
        }
    }
}
