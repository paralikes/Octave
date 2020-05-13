package gg.octave.bot.music

import java.io.*

open class TrackContext(val requester: Long, val requestedChannel: Long) {
    val requesterMention = if (requester != -1L) "<@$requester>" else ""
    val channelMention = if (requestedChannel != -1L) "<#$requestedChannel>" else ""

    fun serialize(stream: ByteArrayOutputStream) {
        val writer = DataOutputStream(stream)
        writer.writeInt(1)
        // 1 => TrackContext
        // 2 => DiscordFMTrackContext
        writer.writeLong(requester)
        writer.writeLong(requestedChannel)
        writer.close() // This invokes flush.
    }

    companion object {
        fun deserialize(stream: ByteArrayInputStream): TrackContext? {
            if (stream.available() == 0) {
                return null
            }

            try {
                val reader = DataInputStream(stream)
                val contextType = reader.readInt()
                val requester = reader.readLong()
                val requestedChannel = reader.readLong()

                val ctx = when (contextType) {
                    1 -> TrackContext(requester, requestedChannel)
                    2 -> {
                        val station = reader.readUTF()
                        DiscordFMTrackContext(station, requester, requestedChannel)
                    }
                    else -> throw IllegalArgumentException("Invalid contextType $contextType!")
                }

                reader.close()
                return ctx
            } catch (e: EOFException) {
                println("End of stream; no user data to be read! Remaining bytes: ${stream.available()}")
                return null
            }
        }
    }
}
