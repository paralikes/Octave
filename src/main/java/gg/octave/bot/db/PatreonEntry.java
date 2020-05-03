package gg.octave.bot.db;

import java.beans.ConstructorProperties;
import java.util.List;

public class PatreonEntry extends ManagedObject {
    private final long timeOfClaim;
    private final List<String> keys;

    @ConstructorProperties({"id", "timeOfClaim", "keys"})
    public PatreonEntry(String id, long timeOfClaim, List<String> keys) {
        super(id, "patreon");
        this.timeOfClaim = timeOfClaim;
        this.keys = keys;
    }

    public List<String> getKeys() {
        return keys;
    }

    public long getTimeOfClaim() {
        return timeOfClaim;
    }
}
