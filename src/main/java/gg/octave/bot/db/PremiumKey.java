package gg.octave.bot.db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.beans.ConstructorProperties;

@JsonIgnoreProperties("expiresBy")
public class PremiumKey extends ManagedObject {
    private final long duration;
    private final Type type;
    private Redeemer redeemer;

    @ConstructorProperties({"id", "type", "duration"})
    public PremiumKey(String id, Type type, long duration) {
        super(id, "keys");
        this.type = type;
        this.duration = duration;
    }

    public Type getType() {
        return type;
    }

    public long getDuration() {
        return duration;
    }

    public Redeemer getRedeemer() {
        return redeemer;
    }

    public PremiumKey setRedeemer(Redeemer redeemer) {
        this.redeemer = redeemer;
        return this;
    }

    @Override
    public String toString() {
        return type + "(" + getId() + ", " + duration + ")";
    }

    public enum Type {
        PREMIUM, // GUILD
        USER,
        PREMIUM_OVERRIDE
    }
}
