package pl.r3.zlecenie.level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import pl.r3.zlecenie.DatabaseManager;
import pl.r3.zlecenie.config.ConfigManager;
import pl.r3.zlecenie.user.User;
import pl.r3.zlecenie.user.UserManager;

import java.util.UUID;

public class LevelManager implements Listener {
    private final DatabaseManager databaseManager;
    private static FileConfiguration config;
    private final UserManager userManager;
    private final User user;

    public LevelManager(DatabaseManager databaseManager, FileConfiguration config, UserManager userManager, User user) {
        this.databaseManager = databaseManager;
        this.userManager = userManager;
        this.user = user;
    }

    public void displayPlayerLevel(Player player) {
        UUID playerId = player.getUniqueId();
        int currentLevel = user.getLevel();
        int currentExp = databaseManager.getPlayerExp(playerId);
        int requiredExpForNextLevel = ConfigManager.getRequiredExpForNextLevel(currentLevel);

        if(requiredExpForNextLevel < 0) {
            return;
        }

        if (currentLevel == ConfigManager.getMaxLevel()) {
            player.setExp(0.999f);
        } else {
            double progressPercentage = (double) currentExp / requiredExpForNextLevel * 100;
            float expProgress = (float) (progressPercentage / 100.0);
            player.setLevel(currentLevel);
            player.setExp(expProgress);
        }
    }

}
