package pl.r3.zlecenie.config;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private static FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public static int getRequiredExpForNextLevel(int nextLevel) {
        if(config.isSet("levels.experience_required." + nextLevel)){
            return config.getInt("levels.experience_required." + nextLevel);
        }
        return -1;
    }
    public static int getMaxLevel() {
        return config.getInt("levels.max_level");
    }
}
