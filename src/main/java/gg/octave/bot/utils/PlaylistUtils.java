package gg.octave.bot.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import gg.octave.bot.Launcher;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PlaylistUtils {
    public static BasicAudioPlaylist decodePlaylist(List<String> encodedTracks, String name) {
        List<AudioTrack> tracks = new CopyOnWriteArrayList<>();

        for (String encodedTrack : encodedTracks)
            tracks.add(toAudioTrack(encodedTrack));

        return new BasicAudioPlaylist(name, tracks, tracks.get(0), false);
    }

    public static BasicAudioPlaylist decodePlaylist(String jsonString) {
        JSONObject object = new JSONObject(jsonString);

        String name = object.getString("name");
        boolean isSearch = object.getBoolean("search");
        int selectedIndex = object.getInt("selected");

        JSONArray encodedTracks = object.getJSONArray("tracks");
        List<AudioTrack> tracks = new ArrayList<>(encodedTracks.length());

        for (Object encodedTrack : encodedTracks) {
            tracks.add(toAudioTrack((String) encodedTrack));
        }

        AudioTrack selectedTrack = selectedIndex > -1
                ? tracks.get(selectedIndex)
                : null;

        return new BasicAudioPlaylist(name, tracks, selectedTrack, isSearch);
    }

    public static String toJsonString(AudioPlaylist playlist) {
        int selectedIndex = playlist.getSelectedTrack() != null
                ? playlist.getTracks().indexOf(playlist.getSelectedTrack())
                : -1;

        JSONArray tracks = new JSONArray();
        playlist.getTracks().stream()
                .map(PlaylistUtils::toBase64String)
                .forEach(tracks::put);

        JSONObject object = new JSONObject();
        object.put("name", playlist.getName());
        object.put("tracks", tracks);
        object.put("search", playlist.isSearchResult());
        object.put("selected", selectedIndex);

        return object.toString();
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
            AudioPlayerManager playerManager = Launcher.INSTANCE.getPlayers().getPlayerManager();
            byte[] b64 = Base64.getDecoder().decode(encoded);
            ByteArrayInputStream bais = new ByteArrayInputStream(b64);
            return playerManager.decodeTrack(new MessageInput(bais)).decodedTrack;
        } catch (Exception e) {
            return null;
        }
    }

    public static String toBase64String(AudioTrack track) {
        AudioPlayerManager playerManager = Launcher.INSTANCE.getPlayers().getPlayerManager();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            playerManager.encodeTrack(new MessageOutput(baos), track);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }
}
