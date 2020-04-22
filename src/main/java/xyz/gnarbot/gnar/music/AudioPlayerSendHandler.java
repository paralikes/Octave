package xyz.gnarbot.gnar.music;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final MutableAudioFrame lastFrame;
    private final ByteBuffer frameBuffer;

    /**
     * @param audioPlayer Audio player to wrap.
     */
    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.lastFrame = new MutableAudioFrame();
        this.frameBuffer = ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize());

        this.lastFrame.setBuffer(frameBuffer);
    }

    @Override
    public boolean canProvide() {
        return audioPlayer.provide(lastFrame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return frameBuffer.flip();
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
