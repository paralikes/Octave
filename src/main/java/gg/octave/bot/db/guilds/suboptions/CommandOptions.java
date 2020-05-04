package gg.octave.bot.db.guilds.suboptions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"allowedChannels", "allowedUsers", "allowedRoles"})
public class CommandOptions {
    private boolean enabled = true;

    @JsonSerialize
    @JsonDeserialize(contentAs = String.class)
    private Set<String> disabledChannels;

    @JsonSerialize
    @JsonDeserialize(contentAs = String.class)
    private Set<String> disabledUsers;

    @JsonSerialize
    @JsonDeserialize(contentAs = String.class)
    private Set<String> disabledRoles;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public Set<String> getDisabledChannels() {
        if (disabledChannels == null) {
            disabledChannels = new HashSet<>();
        }
        return disabledChannels;
    }

    @JsonIgnore
    public Set<String> getDisabledUsers() {
        if (disabledUsers == null) {
            disabledUsers = new HashSet<>();
        }
        return disabledUsers;
    }

    @JsonIgnore
    public Set<String> getDisabledRoles() {
        if (disabledRoles == null) {
            disabledRoles = new HashSet<>();
        }
        return disabledRoles;
    }

    public CommandOptions copy() {
        CommandOptions copy = new CommandOptions();
        copy.enabled = this.enabled;
        copy.disabledChannels = new HashSet<>(this.disabledChannels);
        copy.disabledRoles = new HashSet<>(this.disabledRoles);
        copy.disabledUsers = new HashSet<>(this.disabledUsers);
        return copy;
    }

    @JsonIgnore
    public Set<String> rawDisabledChannels() {
        return disabledChannels;
    }

    @JsonIgnore
    public Set<String> rawDisabledUsers() {
        return disabledUsers;
    }

    @JsonIgnore
    public Set<String> rawDisabledRoles() {
        return disabledRoles;
    }
}
