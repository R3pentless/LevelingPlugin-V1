package pl.r3.levelingplugin.user;

import java.util.UUID;

public class User {
    private UUID uuid;
    private int level;
    private int exp;
    private int highestReward;

    public User() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getHighestReward() {
        return highestReward;
    }

    public void setHighestReward(int highestReward) {
        this.highestReward = highestReward;
    }

    public void claimReward(int rewardNumber) {
        if (rewardNumber > highestReward) {
            highestReward = rewardNumber;
        }
    }
}
