package xyz.gnarbot.gnar.db.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import xyz.gnarbot.gnar.db.ManagedObject;
import xyz.gnarbot.gnar.utils.PlaylistUtils;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlaylist extends ManagedObject {
    @ConstructorProperties("id")
    public MusicPlaylist(String id) {
        super(id, "savedplaylists");
    }

    private String name;
    private List<String> encodedTracks = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getEncodedTracks() {
        return encodedTracks;
    }

    public void replacePlaylist(BasicAudioPlaylist playlist) {
        encodedTracks = PlaylistUtils.encodePlaylist(playlist);
    }

    public void appendTrack(AudioTrack track) throws IOException {
        encodedTracks.add(PlaylistUtils.toBase64String(track));
    }

    public void appendTracks(List<AudioTrack> tracks) {
        tracks.forEach(track -> {
            try {
                PlaylistUtils.toBase64String(track);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
