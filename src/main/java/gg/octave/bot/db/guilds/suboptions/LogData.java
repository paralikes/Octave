package gg.octave.bot.db.guilds.suboptions;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LogData {
    @JsonSerialize(keyAs = String.class, contentAs = Long.class)
    @JsonDeserialize(keyAs = String.class, contentAs = Long.class)
    private Map<String, Long> logChannels;
    //LogType and Channel ID

    @NotNull
    public final Map<String, Long> getChannels() {
        if (logChannels == null) logChannels = new HashMap<>() {
        };
        return logChannels;
    }
}
