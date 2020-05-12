package gg.octave.bot.utils

import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import gg.octave.bot.Launcher.players
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

object PlaylistUtils {
    private val playerManager = players.playerManager

    // Playlists
    fun decodePlaylist(encodedTracks: List<String>, name: String): BasicAudioPlaylist {
        val decoded = encodedTracks.mapNotNull(::toAudioTrack)
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
            val decodedTrack = toAudioTrack(encodedTrack as String) ?: continue
            tracks.add(decodedTrack)
        }

        val selectedTrack = if (selectedIndex > -1) tracks[selectedIndex] else null
        return BasicAudioPlaylist(name, tracks, selectedTrack, isSearch)
    }

    fun toJsonString(playlist: AudioPlaylist): String {
        val selectedIndex = playlist.selectedTrack?.let(playlist.tracks::indexOf) ?: -1
        val tracks = JSONArray()

        for (track in playlist.tracks) {
            val enc = toBase64String(track) ?: continue
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
    fun encodePlaylist(playlist: Collection<AudioTrack>): List<String> = playlist.map(::toBase64String)

    // Tracks
    fun toAudioTrack(encoded: String): AudioTrack {
        val dec = Base64.getDecoder().decode(encoded)
        return ByteArrayInputStream(dec).use {
            playerManager.decodeTrack(MessageInput(it)).decodedTrack
        }
    }

    fun toBase64String(track: AudioTrack): String {
        return ByteArrayOutputStream().use {
            playerManager.encodeTrack(MessageOutput(it), track)
            Base64.getEncoder().encodeToString(it.toByteArray())
        }
    }

    // Misc
    private fun <T: Throwable, R> suppress(exception: Class<out T>, block: () -> R): R? {
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
