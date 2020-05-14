package gg.octave.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

class ExtendedAudioPlayerManager(private val dapm: AudioPlayerManager = DefaultAudioPlayerManager()) : AudioPlayerManager by dapm {
    /**
     * @return a base64 encoded string containing the track data.
     */
    fun encodeTrack(track: AudioTrack): String {
        val baos = ByteArrayOutputStream()
        dapm.encodeTrack(MessageOutput(baos), track)

        track.userData?.takeIf { it is TrackContext }?.let {
            (it as TrackContext).serialize(baos) // Write our user data to the stream.
        }

        val encoded = Base64.getEncoder().encodeToString(baos.toByteArray())
        baos.close()
        return encoded
    }

    /**
     * @return An AudioTrack with possibly-null user data.
     */
    fun decodeTrack(base64: String): AudioTrack? {
        val decoded = Base64.getDecoder().decode(base64)
        val bais = ByteArrayInputStream(decoded)
        val track = dapm.decodeTrack(MessageInput(bais))

        val audioTrack = track?.decodedTrack
            ?: return null

        val trackContext = TrackContext.deserialize(bais)

        if (trackContext != null) {
            audioTrack.userData = trackContext
        }

        return audioTrack
    }

    fun decodePlaylist(encodedTracks: List<String>, name: String): BasicAudioPlaylist {
        val decoded = encodedTracks.mapNotNull(::decodeMaybeNullAudioTrack)
        return BasicAudioPlaylist(name, decoded, decoded[0], false)
    }

    fun toJsonString(playlist: AudioPlaylist): String {
        val selectedIndex = playlist.selectedTrack?.let(playlist.tracks::indexOf) ?: -1
        val tracks = JSONArray()

        for (track in playlist.tracks) {
            val enc = encodeAudioTrack(track)
            tracks.put(enc)
        }

        return JSONObject().apply {
            put("name", playlist.name)
            put("tracks", tracks)
            put("search", playlist.isSearchResult)
            put("selected", selectedIndex)
        }.toString()
    }

    fun decodePlaylist(jsonString: String): BasicAudioPlaylist {
        val jo = JSONObject(jsonString)

        val name = jo.getString("name")
        val isSearch = jo.getBoolean("search")
        val selectedIndex = jo.getInt("selected")

        val encodedTracks = jo.getJSONArray("tracks")
        val tracks = mutableListOf<AudioTrack>()

        for (encodedTrack in encodedTracks) {
            val decodedTrack = decodeAudioTrack(encodedTrack as String)
            tracks.add(decodedTrack)
        }

        val selectedTrack = if (selectedIndex > -1) tracks[selectedIndex] else null
        return BasicAudioPlaylist(name, tracks, selectedTrack, isSearch)
    }

    fun encodePlaylist(playlist: BasicAudioPlaylist) = encodePlaylist(playlist.tracks)
    fun encodePlaylist(playlist: Collection<AudioTrack>): List<String> = playlist.map(::encodeAudioTrack)

    fun encodeAudioTrack(track: AudioTrack) = encodeTrack(track)

    // This is used at the top of the file. Don't ask :^)
    fun decodeMaybeNullAudioTrack(encoded: String) = decodeTrack(encoded)
    fun decodeAudioTrack(encoded: String) = decodeTrack(encoded)!!
}
