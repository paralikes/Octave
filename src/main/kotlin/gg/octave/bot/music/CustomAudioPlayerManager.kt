package gg.octave.bot.music

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.DecodedTrackHolder
import java.io.*
import kotlin.experimental.and

class CustomAudioPlayerManager : DefaultAudioPlayerManager() {
    private val TRACK_INFO_VERSIONED = 1
    private val TRACK_INFO_VERSION = 3

    @Throws(IOException::class)
    override fun encodeTrack(stream: MessageOutput, track: AudioTrack) {
        val output = stream.startMessage()
        output.write(TRACK_INFO_VERSION)
        val trackInfo = track.info
        output.writeUTF(trackInfo.title)
        output.writeUTF(trackInfo.author)
        output.writeLong(trackInfo.length)
        output.writeUTF(trackInfo.identifier)
        output.writeBoolean(trackInfo.isStream)
        DataFormatTools.writeNullableText(output, trackInfo.uri)
        encodeTrackDetails(track, output)
        encodeUserData(track, output)
        output.writeLong(track.position)
        stream.commitMessage(TRACK_INFO_VERSIONED)
    }

    @Throws(IOException::class)
    override fun decodeTrack(stream: MessageInput): DecodedTrackHolder? {
        val input = stream.nextMessage() ?: return null
        val version = if (stream.messageFlags and TRACK_INFO_VERSIONED != 0) (input.readByte() and 0xFF.toByte()) else 1
        val trackInfo = AudioTrackInfo(input.readUTF(), input.readUTF(), input.readLong(), input.readUTF(),
                input.readBoolean(), if (version >= 2) DataFormatTools.readNullableText(input) else null)
        val track = decodeTrackDetails(trackInfo, input)
        val position = input.readLong()
        val userData = if(version >= 3) input.readUTF() else ""

        if (track != null) {
            track.userData = userData
            track.position = position
        }

        stream.skipRemainingBytes()
        return DecodedTrackHolder(track)
    }

    /**
     * Encodes an audio track to a byte array. Does not include AudioTrackInfo in the buffer.
     * @param track The track to encode
     * @return The bytes of the encoded data
     */
    override fun encodeTrackDetails(track: AudioTrack): ByteArray? {
        return try {
            val byteOutput = ByteArrayOutputStream()
            val output: DataOutput = DataOutputStream(byteOutput)
            encodeTrackDetails(track, output)
            byteOutput.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun encodeUserData(track: AudioTrack): ByteArray? {
        return try {
            val byteOutput = ByteArrayOutputStream()
            val output: DataOutput = DataOutputStream(byteOutput)
            encodeUserData(track, output)
            byteOutput.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    private fun encodeUserData(track: AudioTrack, output: DataOutput) {
        val sourceManager = track.sourceManager
        output.writeUTF(track.userData.toString())
        sourceManager.encodeTrack(track, output)
    }

    @Throws(IOException::class)
    private fun encodeTrackDetails(track: AudioTrack, output: DataOutput) {
        val sourceManager = track.sourceManager
        output.writeUTF(sourceManager.sourceName)
        sourceManager.encodeTrack(track, output)
    }

    /**
     * Decodes an audio track from a byte array.
     * @param trackInfo Track info for the track to decode
     * @param buffer Byte array containing the encoded track
     * @return Decoded audio track
     */
    override fun decodeTrackDetails(trackInfo: AudioTrackInfo, buffer: ByteArray?): AudioTrack? {
        return try {
            val input: DataInput = DataInputStream(ByteArrayInputStream(buffer))
            decodeTrackDetails(trackInfo, input)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    private fun decodeTrackDetails(trackInfo: AudioTrackInfo, input: DataInput): AudioTrack? {
        val sourceName = input.readUTF()
        for (sourceManager in sourceManagers) {
            if (sourceName == sourceManager.sourceName) {
                return sourceManager.decodeTrack(trackInfo, input)
            }
        }
        return null
    }
}