package gg.octave.bot.db.guilds.suboptions;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class IgnoredData {
    private Set<String> channels;
    private Set<String> users;
    private Set<String> roles;

    @NotNull
    public final Set<String> getChannels() {
        if (channels == null) {
            channels = new HashSet<>();
        }
        return channels;
    }

    @NotNull
    public final Set<String> getUsers() {
        if (users == null) {
            users = new HashSet<>();
        }
        return users;
    }

    @NotNull
    public final Set<String> getRoles() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        return roles;
    }
}
