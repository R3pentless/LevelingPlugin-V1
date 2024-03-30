package pl.r3.zlecenie.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.r3.zlecenie.utills.DatabaseManager;
import pl.r3.zlecenie.user.UserManager;

public class PlayerDisconnectListener implements Listener {

    private UserManager userManager;
    private DatabaseManager databaseManager;

    public PlayerDisconnectListener(DatabaseManager databaseManager, UserManager userManager) {
        this.databaseManager = databaseManager;
        this.userManager = userManager;
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (databaseManager != null && userManager != null) {
            userManager.sendUserToDatabase(player, databaseManager);
            userManager.removeUser(player.getUniqueId());
        }
    }
}
