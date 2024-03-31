package pl.r3.levelingplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.r3.levelingplugin.config.ConfigManager;
import pl.r3.levelingplugin.gui.GuiCommand;
import pl.r3.levelingplugin.gui.GuiListener;
import pl.r3.levelingplugin.gui.GuiManager;
import pl.r3.levelingplugin.level.ExpManager;
import pl.r3.levelingplugin.level.LevelListener;
import pl.r3.levelingplugin.level.LevelManager;
import pl.r3.levelingplugin.listener.PlayerDisconnectListener;
import pl.r3.levelingplugin.listener.PlayerJoinListener;
import pl.r3.levelingplugin.user.UserManager;
import pl.r3.levelingplugin.utills.DatabaseManager;

import java.util.logging.Level;
import java.util.stream.Stream;

public final class LevelingPlugin extends JavaPlugin {
    private DatabaseManager databaseManager;
    private LevelManager lvlManager;
    private ExpManager expManager;
    private PlayerJoinListener playerJoinListener;
    private PlayerDisconnectListener playerDisconnectListener;
    private LevelListener levelListener;
    private GuiListener guiListener;
    private ConfigManager configManager;
    private GuiManager guiManager;
    private UserManager userManager;
    private int autoSave;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initDatabase();

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

        userManager = new UserManager();
        guiManager = new GuiManager(this, getConfig(), userManager);
        configManager = new ConfigManager(getConfig());

        lvlManager = new LevelManager(databaseManager, userManager);
        expManager = new ExpManager(this, lvlManager, userManager, configManager);
        guiListener = new GuiListener(this, databaseManager, userManager, guiManager);
        playerJoinListener = new PlayerJoinListener(databaseManager, userManager);
        playerDisconnectListener = new PlayerDisconnectListener(databaseManager, userManager);
        levelListener = new LevelListener();

        Stream.of(
                lvlManager,
                guiListener,
                expManager,
                playerJoinListener,
                playerDisconnectListener,
                levelListener
        ).forEach(listener -> getServer().getPluginManager().registerEvents((Listener) listener, this));

        getCommand("poziomy").setExecutor(new GuiCommand(this, databaseManager, guiManager));

        reloadConfig();
        int autoSaveDelay = getConfig().getInt("autosave.delay");

        if (autoSave != 0) {
            Bukkit.getScheduler().cancelTask(autoSave);
        }

        autoSave = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                userManager.sendUserToDatabase(player, databaseManager);
                if(getConfig().getBoolean("autosave.autosave_message") == true){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("autosave.message")));
                }

            });
        }, 0, autoSaveDelay);

        Bukkit.getOnlinePlayers().forEach(player -> {
            userManager.loadUserFromDatabase(player, databaseManager);
        });
    }



    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(autoSave);

        Bukkit.getOnlinePlayers().forEach(player -> {
            userManager.sendUserToDatabase(player, databaseManager);
        });


        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }

    private void initDatabase() {
        String host = getConfig().getString("database.host");
        int port = getConfig().getInt("database.port");
        String database = getConfig().getString("database.database");
        String username = getConfig().getString("database.username");
        String password = getConfig().getString("database.password");

        databaseManager = new DatabaseManager(host, port, database, username, password);
    }

}
