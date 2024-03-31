package pl.r3.levelingplugin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.r3.levelingplugin.utills.DatabaseManager;
import pl.r3.levelingplugin.user.UserManager;

public class PlayerJoinListener implements Listener {
    private UserManager userManager;
    private DatabaseManager databaseManager;

    public PlayerJoinListener(DatabaseManager databaseManager, UserManager userManager) {
        this.databaseManager = databaseManager;
        this.userManager = userManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!databaseManager.playerExists(player.getUniqueId())) {
            databaseManager.insertPlayerData(player.getPlayerListName(), player.getUniqueId());
        }

        userManager.loadUserFromDatabase(player, databaseManager);
    }
}
