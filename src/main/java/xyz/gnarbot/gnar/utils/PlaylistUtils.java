package xyz.gnarbot.gnar.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import xyz.gnarbot.gnar.Bot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PlaylistUtils {
    public static BasicAudioPlaylist decodePlaylist(List<String> encodedTracks, String name) {
        List<AudioTrack> tracks = new CopyOnWriteArrayList<>();

        for(String encodedTrack : encodedTracks)
            tracks.add(toAudioTrack(encodedTrack));

        return new BasicAudioPlaylist(name, tracks, tracks.get(0), false);
    }

    public static List<String> encodePlaylist(Queue<AudioTrack> playlist) {
        return playlist.stream()
                .filter(Objects::nonNull)
                .map(PlaylistUtils::toBase64String)
                .collect(Collectors.toList());
    }

    public static List<String> encodePlaylist(BasicAudioPlaylist playlist) {
        Queue<AudioTrack> tracks = new LinkedList<>(playlist.getTracks());
        return encodePlaylist(tracks);
    }

    public static AudioTrack toAudioTrack(String encoded) {
        try {
            AudioPlayerManager playerManager = Bot.getInstance().getPlayers().getPlayerManager();
            byte[] b64 = Base64.getDecoder().decode(encoded);
            ByteArrayInputStream bais = new ByteArrayInputStream(b64);
            return playerManager.decodeTrack(new MessageInput(bais)).decodedTrack;
        } catch (Exception e) {
            return null;
        }
    }

    public static String toBase64String(AudioTrack track) {
        AudioPlayerManager playerManager = Bot.getInstance().getPlayers().getPlayerManager();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            playerManager.encodeTrack(new MessageOutput(baos), track);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }
}
