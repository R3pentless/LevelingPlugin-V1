package pl.r3.zlecenie.user;

import org.bukkit.entity.Player;

public class User {
    private Player player;
    private int level;
    private int exp;
    private int highestRewardReceived;

    public User(Player player, int level, int exp, int highestRewardReceived) {
        this.player = player;
        this.level = level;
        this.exp = exp;
        this.highestRewardReceived = highestRewardReceived;
    }

    public Player getPlayer() {
        return player;
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

    public int getHighestRewardReceived() {
        return highestRewardReceived;
    }
}
