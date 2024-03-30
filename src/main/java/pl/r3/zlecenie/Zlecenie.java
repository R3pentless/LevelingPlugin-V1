package pl.r3.zlecenie;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.r3.zlecenie.config.ConfigManager;
import pl.r3.zlecenie.gui.GuiCommand;
import pl.r3.zlecenie.gui.GuiListener;
import pl.r3.zlecenie.gui.GuiManager;
import pl.r3.zlecenie.level.ExpManager;
import pl.r3.zlecenie.level.LevelListener;
import pl.r3.zlecenie.level.LevelManager;
import pl.r3.zlecenie.listener.PlayerDisconnectListener;
import pl.r3.zlecenie.listener.PlayerJoinListener;
import pl.r3.zlecenie.user.UserManager;
import pl.r3.zlecenie.utills.DatabaseManager;

import java.util.logging.Level;
import java.util.stream.Stream;

public final class Zlecenie extends JavaPlugin {
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
    private int taskId;

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

        // Register GUI command
        getCommand("zlecenie").setExecutor(new GuiCommand(this, databaseManager, guiManager));

        // Schedule task
        int delay = 6000; // 5 minutes (20 ticks per second)
        int period = 6000; // 5 minutes
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                userManager.sendUserToDatabase(player, databaseManager);
            });
        }, delay, period);
        Bukkit.getOnlinePlayers().forEach(player -> {
            userManager.loadUserFromDatabase(player, databaseManager);
        });
    }


    @Override
    public void onDisable() {
        // Cancel the scheduled task
        Bukkit.getScheduler().cancelTask(taskId);

        Bukkit.getOnlinePlayers().forEach(player -> {
            userManager.sendUserToDatabase(player, databaseManager);
        });
        // Disconnect from the database
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

        // Initialize DatabaseManager
        databaseManager = new DatabaseManager(host, port, database, username, password);
    }

}
