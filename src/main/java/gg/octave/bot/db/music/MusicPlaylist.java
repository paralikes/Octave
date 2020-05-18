package gg.octave.bot.db.music;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import gg.octave.bot.Launcher;
import gg.octave.bot.db.ManagedObject;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @JsonIgnore
    public BasicAudioPlaylist toLavaPlaylist() {
        return Launcher.INSTANCE.getPlayers().getPlayerManager().decodePlaylist(encodedTracks, name);
    }

    @JsonIgnore
    public void replacePlaylist(BasicAudioPlaylist playlist) {
        encodedTracks = Launcher.INSTANCE.getPlayers().getPlayerManager().encodePlaylist(playlist);
    }

    @JsonIgnore
    public MusicPlaylist replacePlaylist(List<String> encodedTracks) {
        this.encodedTracks = encodedTracks;
        return this;
    }

    @JsonIgnore
    public void appendTrack(AudioTrack track) throws IOException {
        encodedTracks.add(Launcher.INSTANCE.getPlayers().getPlayerManager().encodeAudioTrack(track));
    }

    @JsonIgnore
    public void appendTracks(List<AudioTrack> tracks) {
        tracks.forEach(Launcher.INSTANCE.getPlayers().getPlayerManager()::encodeAudioTrack);
    }
}
