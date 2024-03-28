package pl.r3.zlecenie;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;
import java.util.logging.Level;

public final class Zlecenie extends JavaPlugin implements Listener {

    private GuiManager guiManager;
    private DatabaseManager databaseManager;
    private LevelManager lvlManager;
    private ExpManager expManager; // Add reference to ExpManager

    @Override
    public void onEnable() {
        // Call the method to create configuration files if they don't exist
        ConfigCreator.createConfigFile(this);

        // Save default config
        saveDefaultConfig();

        // Load database configuration
        File databaseFile = new File(getDataFolder(), "database.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(databaseFile);

        // Retrieve database connection parameters
        String host = config.getString("database.host");
        int port = config.getInt("database.port");
        String database = config.getString("database.database");
        String username = config.getString("database.username");
        String password = config.getString("database.password");

        // Initialize DatabaseManager
        databaseManager = new DatabaseManager(host, port, database, username, password);

        // Connect to the database
        if (databaseManager.connect()) {
            boolean tableCreated = databaseManager.createTable();
            boolean tableExist = databaseManager.tableExists("player_data");
            if (tableCreated && !tableExist) {
                getLogger().log(Level.INFO, "Table 'player_data' created successfully.");
            } else {
                getLogger().log(Level.INFO, "Table 'player_data' already exists.");
            }
        } else {
            getLogger().log(Level.SEVERE, "Failed to connect to the database. Plugin functionality may be affected.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize LevelManager after databaseManager
        lvlManager = new LevelManager(databaseManager, getConfig());
        getServer().getPluginManager().registerEvents(lvlManager, this);

        // Initialize GuiManager after databaseManager
        guiManager = new GuiManager(this, databaseManager);
        getServer().getPluginManager().registerEvents(guiManager, this);

        // Initialize ExpManager after databaseManager and lvlManager
        expManager = new ExpManager(this, lvlManager, databaseManager);
        getServer().getPluginManager().registerEvents(expManager, this);

        // Register listener for player join event
        getServer().getPluginManager().registerEvents(this, this);
    }


    @Override
    public void onDisable(){
        databaseManager.disconnect();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        guiManager.openRewardsGUI(player);
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();

        // Check if player exists in the database
        if (!databaseManager.playerExists(playerUUID)) {
            // If not, insert player data into the database
            databaseManager.insertPlayerData(playerName, playerUUID);
            getLogger().log(Level.INFO, "Player data for " + playerName + " inserted into the database.");
        }
    }
}
