package xyz.gnarbot.gnar.music

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
    private val frameBuffer = ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize())
    private val lastFrame = MutableAudioFrame().apply { setBuffer(frameBuffer) }

    override fun canProvide() = audioPlayer.provide(lastFrame)
    override fun provide20MsAudio() = frameBuffer.flip()
    override fun isOpus() = true
}
