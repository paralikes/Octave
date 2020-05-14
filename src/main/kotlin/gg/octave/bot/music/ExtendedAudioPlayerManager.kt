package gg.octave.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
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
}
