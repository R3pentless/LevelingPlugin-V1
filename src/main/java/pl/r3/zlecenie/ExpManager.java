package pl.r3.zlecenie;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.UUID;

public class ExpManager implements Listener {
    DecimalFormat df = new DecimalFormat("0.00");

    private final JavaPlugin plugin;
    private final LevelManager levelManager;
    private final DatabaseManager databaseManager;

    public ExpManager(JavaPlugin plugin, LevelManager levelManager, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.levelManager = levelManager;
        this.databaseManager = databaseManager;
    }

    private void awardExp(Player player, int exp) {
        UUID playerId = player.getUniqueId();
        int level = databaseManager.getPlayerLevel(playerId);
        int currentExp = databaseManager.getPlayerExp(playerId);
        int requiredExp = levelManager.getRequiredExpForNextLevel(playerId, level);
        currentExp += exp;
        double progress = (double) currentExp / requiredExp * 100;
        String progressString = df.format(progress).replace(',', '.');
        double progressPercentage = Double.parseDouble(progressString);
        if (level < levelManager.getMaxLevel()) {
            String expMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages_level.received_exp")
                    .replace("%exp%", String.valueOf(exp)).replace("%currentexp%", String.valueOf(currentExp)).replace("%requiredexp%", String.valueOf(requiredExp)).replace("%percent%", String.valueOf(progressPercentage)));
            sendActionBar(player, expMessage);
            databaseManager.updatePlayerExp(playerId, currentExp);
        }
        while (currentExp >= requiredExp && level < levelManager.getMaxLevel()) {
            level++;
            requiredExp = levelManager.getRequiredExpForNextLevel(playerId, level);
            String levelMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages_level.reached_level")
                    .replace("%level%", String.valueOf(level)));
            player.sendMessage(levelMessage);
            databaseManager.updatePlayerExp(playerId, 0);
            databaseManager.updatePlayerLevel(playerId, level);
        }
        if (level < levelManager.getMaxLevel()) {
            levelManager.displayPlayerLevel(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        if (material.isBlock()) {
            int exp = getBlockExp(material);
            if (exp > 0) {
                awardExp(player, exp);
            }
        }
    }

    private int getBlockExp(Material material) {
        // Retrieve experience from the configuration
        ConfigurationSection blockExpSection = plugin.getConfig().getConfigurationSection("exp.activities.blocks");
        if (blockExpSection != null && blockExpSection.contains(material.toString())) {
            return blockExpSection.getInt(material.toString());
        }
        return 0; // Return 0 if the material is not found in the configuration
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() != null && entity.getKiller() instanceof Player) {
            Player player = entity.getKiller();
            EntityType entityType = entity.getType();
            int exp = getEntityExp(entityType);
            if (exp > 0) {
                awardExp(player, exp);
            }
        }
    }

    private int getEntityExp(EntityType entityType) {
        ConfigurationSection entityExpSection = plugin.getConfig().getConfigurationSection("exp.activities.entities");
        if (entityExpSection != null && entityExpSection.contains(entityType.name())) {
            return entityExpSection.getInt(entityType.name());
        }
        return 0;
    }


    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

}
