package pl.r3.zlecenie.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.r3.zlecenie.DatabaseManager;
import pl.r3.zlecenie.user.UserData;

import java.util.UUID;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getLogger;

public class PlayerJoinListener implements Listener {

    private final DatabaseManager databaseManager;

    public PlayerJoinListener(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();

        if (!databaseManager.playerExists(playerUUID)) {
            databaseManager.insertPlayerData(playerName, playerUUID);
            getLogger().log(Level.INFO, "Player data for " + playerName + " inserted into the database.");
        } else {
            int level = databaseManager.getPlayerLevel(playerUUID);
            int exp = databaseManager.getPlayerExp(playerUUID);
            int highestRewardReceived = databaseManager.getHighestRewardReceived(playerUUID);


        }
    }
}
