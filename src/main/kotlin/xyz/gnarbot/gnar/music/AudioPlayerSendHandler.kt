package xyz.gnarbot.gnar.music

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
    private val lastFrame: MutableAudioFrame = MutableAudioFrame()
    private val frameBuffer: ByteBuffer = ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize())

    init {
        lastFrame.setBuffer(frameBuffer)
    }
    
    override fun canProvide(): Boolean {
        return audioPlayer.provide(lastFrame)
    }

    override fun provide20MsAudio(): ByteBuffer? {
        return frameBuffer.flip()
    }

    override fun isOpus(): Boolean {
        return true
    }

}