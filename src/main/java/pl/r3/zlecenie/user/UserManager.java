package pl.r3.zlecenie.user;

import pl.r3.zlecenie.DatabaseManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserManager {

    private final DatabaseManager databaseManager;
    private static Map<UUID, User> users = new HashMap<>();

    public UserManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Optional<User> getUserByUUID(UUID playerId) {
        return Optional.ofNullable(users.get(playerId));
    }

    public void addUser(UUID playerId, User user) {
        users.put(playerId, user);
    }

    public void updateUser(UUID playerId, User updatedUser) {
        if (users.containsKey(playerId)) {
            users.put(playerId, updatedUser);
        }
    }

    public void updateDatabaseFromCache() {
        for (Map.Entry<UUID, User> entry : users.entrySet()) {
            UUID playerId = entry.getKey();
            User user = entry.getValue();
            int level = user.getLevel();
            int exp = user.getExp();
            int highestRewardReceived = user.getHighestRewardReceived();

            // Update the database with the user's data
            databaseManager.updatePlayerLevel(playerId, level);
            databaseManager.updatePlayerExp(playerId, exp);
            databaseManager.updateHighestRewardReceived(playerId, highestRewardReceived);
        }
    }
}
