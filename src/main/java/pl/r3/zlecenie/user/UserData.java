package pl.r3.zlecenie.user;

import java.util.UUID;

public class UserData {
    private final UUID uuid;
    private int level;
    private int exp;
    private int highestReward;

    public UserData(UUID uuid) {
        this.uuid = uuid;
    }
    public UUID getUuid() {
        return uuid;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    private int getExp(){
        return exp;
    }
    private void setExp(int exp){
        this.exp = exp;
    }
    private int getHighestReward(){
        return highestReward;
    }
    private void setHighestReward(int highestReward){
        this.highestReward = highestReward;
    }
}
