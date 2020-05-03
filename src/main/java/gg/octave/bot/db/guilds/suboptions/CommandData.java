package gg.octave.bot.db.guilds.suboptions;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CommandData {
    private String prefix;
    private String djRole;
    private boolean autoDelete = false;
    private boolean adminBypass = false;
    private boolean djOnlyMode;

    @JsonSerialize(keyAs = Integer.class, contentAs = CommandOptions.class)
    private Map<Integer, CommandOptions> options;

    @JsonSerialize(keyAs = Integer.class, contentAs = CommandOptions.class)
    private Map<Integer, CommandOptions> categoryOptions;

    @Nullable
    public String getPrefix() {
        return prefix;
    }

    @Nullable
    public String getDJRole() {
        return djRole;
    }

    public String setDJRole(String djRole) {
        return this.djRole = djRole;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public boolean isAdminBypass() {
        return adminBypass;
    }

    public void setAdminBypass(boolean adminBypass) {
        this.adminBypass = adminBypass;
    }

    public Map<Integer, CommandOptions> getOptions() {
        if (options == null) {
            options = new HashMap<>();
        }
        return options;
    }

    public Map<Integer, CommandOptions> getCategoryOptions() {
        if (categoryOptions == null) {
            categoryOptions = new HashMap<>();
        }
        return categoryOptions;
    }

    public boolean isDjOnlyMode() {
        return djOnlyMode;
    }

    public void setDjOnlyMode(boolean djOnlyMode) {
        this.djOnlyMode = djOnlyMode;
    }
}
