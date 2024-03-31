package pl.r3.levelingplugin.level;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import pl.r3.levelingplugin.utills.DatabaseManager;
import pl.r3.levelingplugin.config.ConfigManager;
import pl.r3.levelingplugin.user.User;
import pl.r3.levelingplugin.user.UserManager;

public class LevelManager implements Listener {
    private DatabaseManager databaseManager;
    private UserManager userManager;

    public LevelManager(DatabaseManager databaseManager, UserManager userManager) {
        this.databaseManager = databaseManager;
        this.userManager = userManager;
    }

    public void displayPlayerLevel(Player player) {
        User user = userManager.getUserData(player.getUniqueId());

        if (user != null) {
            int currentLevel = user.getLevel();
            int currentExp = user.getExp();
            int requiredExpForNextLevel = ConfigManager.getRequiredExpForNextLevel(currentLevel);

            if (requiredExpForNextLevel < 0) {
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
}
