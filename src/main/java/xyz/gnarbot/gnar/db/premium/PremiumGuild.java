package xyz.gnarbot.gnar.db.premium;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import xyz.gnarbot.gnar.Bot;
import xyz.gnarbot.gnar.db.ManagedObject;

import java.beans.ConstructorProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class PremiumGuild extends ManagedObject {
    // The ID of the user that enabled premium for the guild.
    @JsonSerialize
    @JsonDeserialize
    private String redeemer;

    // The epoch timestamp of when premium status expires.
    @JsonSerialize
    @JsonDeserialize
    private long added;

    @ConstructorProperties("id")
    public PremiumGuild(String id) {
        super(id, "premiumguilds");
    }

    @JsonIgnore
    public PremiumGuild setRedeemer(String redeemer) {
        this.redeemer = redeemer;
        return this;
    }

    @JsonIgnore
    public PremiumGuild setAdded(long added) {
        this.added = added;
        return this;
    }

    @JsonIgnore
    public String getRedeemerId() {
        return redeemer;
    }

    @JsonIgnore
    public PremiumUser getRedeemer() {
        return Bot.getInstance().db().getPremiumUser(getRedeemerId());
    }

    @JsonIgnore
    public int getQueueSizeQuota() {
        double pledgeAmount = getRedeemer().getPledgeAmount();
        if (Bot.getInstance().getConfiguration().getAdmins().contains(Long.parseLong(getId())) || pledgeAmount >= 10) {
            return Integer.MAX_VALUE;
        } else if (pledgeAmount >= 5) {
            return 500;
        } else {
            return Bot.getInstance().getConfiguration().getQueueLimit();
        }
    }

    @JsonIgnore
    public long getSongLengthQuota() {
        double pledgeAmount = getRedeemer().getPledgeAmount();
        if (Bot.getInstance().getConfiguration().getAdmins().contains(Long.parseLong(getId()))) {
            return Integer.MAX_VALUE;
        } else if (pledgeAmount >= 10) {
            return TimeUnit.MINUTES.toMillis(720);
        } else if (pledgeAmount >= 5) {
            return TimeUnit.MINUTES.toMillis(360);
        } else {
            return Bot.getInstance().getConfiguration().getDurationLimit().toMillis();
        }
    }


    @JsonIgnore
    public long getDaysSinceAdded() {
        Instant current = Instant.now();
        Instant then = Instant.ofEpochMilli(added);

        return Duration.between(then, current).toDays();
    }
}
