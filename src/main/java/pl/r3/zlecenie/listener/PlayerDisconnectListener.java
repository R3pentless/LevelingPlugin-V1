package pl.r3.zlecenie.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.r3.zlecenie.DatabaseManager;
import pl.r3.zlecenie.user.UserData;

import java.util.Optional;
import java.util.UUID;

public class PlayerDisconnectListener implements Listener {

    private final UserManager userManager;
    private final DatabaseManager databaseManager;

    public PlayerDisconnectListener(UserManager userManager, DatabaseManager databaseManager) {
        this.userManager = userManager;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        // Retrieve the player's UUID
        UUID playerId = event.getPlayer().getUniqueId();

        // Retrieve the user from the user manager
        Optional<UserData> userOptional = userManager.getUserByUUID(playerId);

        // If the user exists, update their data in the database
        userOptional.ifPresent(user -> {
            databaseManager.updatePlayerLevel(playerId, user.getLevel());
            databaseManager.updatePlayerExp(playerId, user.getExp());
            databaseManager.updateHighestRewardReceived(playerId, user.getHighestRewardReceived());
        });
    }
}
