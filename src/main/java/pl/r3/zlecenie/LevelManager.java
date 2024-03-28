package pl.r3.zlecenie;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.UUID;

public class LevelManager implements Listener {
    private final DatabaseManager databaseManager;
    private final FileConfiguration config; // Add a reference to the plugin configuration

    public LevelManager(DatabaseManager databaseManager, FileConfiguration config) {
        this.databaseManager = databaseManager;
        this.config = config; // Initialize the configuration
    }

    public void displayPlayerLevel(Player player) {
        UUID playerId = player.getUniqueId();
        int currentLevel = databaseManager.getPlayerLevel(playerId);
        int currentExp = databaseManager.getPlayerExp(playerId);
        int requiredExpForNextLevel = getRequiredExpForNextLevel(playerId, currentLevel);

        if (currentLevel == getMaxLevel()) {
            player.setExp(0.999f);
        } else {
            double progressPercentage = (double) currentExp / requiredExpForNextLevel * 100;
            float expProgress = (float) (progressPercentage / 100.0);
            player.setLevel(currentLevel);
            player.setExp(expProgress);
        }
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        event.setAmount(0);
    }

    public int getRequiredExpForNextLevel(UUID playerUUID, int nextLevel) {
        return config.getInt("levels.experience_required." + nextLevel);
    }
    public int getMaxLevel() {
        return config.getInt("levels.max_level");
    }

}
