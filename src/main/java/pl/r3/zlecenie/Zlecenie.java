package pl.r3.zlecenie;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import pl.r3.zlecenie.config.ConfigManager;
import pl.r3.zlecenie.gui.GuiListener;
import pl.r3.zlecenie.gui.GuiManager;
import pl.r3.zlecenie.level.LevelListener;
import pl.r3.zlecenie.level.LevelManager;
import pl.r3.zlecenie.listener.PlayerJoinListener;
import pl.r3.zlecenie.user.User;
import pl.r3.zlecenie.user.UserManager;

import java.util.logging.Level;
import java.util.stream.Stream;

public final class Zlecenie extends JavaPlugin {
    private DatabaseManager databaseManager;
    private LevelManager lvlManager;
    private ExpManager expManager;
    private PlayerJoinListener playerJoinListener;
    private LevelListener levelListener;
    private UserManager userManager;
    private GuiListener guiListener;
    private ConfigManager configManager;
    private GuiManager guiManager;
    private User user;
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

        // Initialize managers
        guiManager = new GuiManager(this, getConfig());
        userManager = new UserManager(databaseManager);
        guiListener = new GuiListener(this, databaseManager);
        lvlManager = new LevelManager(databaseManager, getConfig(), userManager, user);
        guiListener = new GuiListener(this, databaseManager);
        expManager = new ExpManager(this, lvlManager, userManager, configManager);
        playerJoinListener = new PlayerJoinListener(databaseManager, userManager);
        levelListener = new LevelListener();

        // Register event listeners
        Stream.of(
                lvlManager,
                guiListener,
                expManager,
                playerJoinListener,
                levelListener
        ).forEach(listener -> getServer().getPluginManager().registerEvents((Listener) listener, this));

        getCommand("zlecenie").setExecutor(new GuiCommand(this, databaseManager, guiManager));

        int delay = 6000; // 5 minutes (20 ticks per second)
        int period = 6000; // 5 minutes
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            userManager.updateDatabaseFromCache();
        }, delay, period);
    }

    @Override
    public void onDisable() {
        // Cancel the scheduled task
        Bukkit.getScheduler().cancelTask(taskId);

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
