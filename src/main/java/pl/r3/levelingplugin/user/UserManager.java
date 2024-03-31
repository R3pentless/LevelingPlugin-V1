package pl.r3.levelingplugin.user;

import org.bukkit.entity.Player;
import pl.r3.levelingplugin.utills.DatabaseManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {

    private Map<UUID, User> userData;

    public UserManager() {
        this.userData = new HashMap<>();
    }

    public void loadUserFromDatabase(Player player, DatabaseManager databaseManager) {
        UUID playerId = player.getUniqueId();
        if (!userData.containsKey(playerId)) {
            int playerLevel = databaseManager.getPlayerLevel(playerId);
            int playerExp = databaseManager.getPlayerExp(playerId);
            int highestRewardReceived = databaseManager.getHighestRewardReceived(playerId);

            User user = new User();
            user.setUuid(playerId);
            user.setLevel(playerLevel);
            user.setExp(playerExp);
            user.setHighestReward(highestRewardReceived);

            userData.put(playerId, user);
        }
    }

    public void sendUserToDatabase(Player player, DatabaseManager databaseManager) {
        UUID playerId = player.getUniqueId();
        User user = userData.get(playerId);
        if (user != null) {
            databaseManager.updatePlayerLevel(playerId, user.getLevel());
            databaseManager.updatePlayerExp(playerId, user.getExp());
            databaseManager.updateHighestRewardReceived(playerId, user.getHighestReward());
        }
    }

    public void updateUser(UUID playerId, User user) {
        userData.put(playerId, user);
    }

    public User getUserData(UUID playerId) {
        return userData.get(playerId);
    }

    public void removeUser(UUID playerId) {
        userData.remove(playerId);
    }

}
