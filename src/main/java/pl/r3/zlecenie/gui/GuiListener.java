package pl.r3.zlecenie.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.r3.zlecenie.DatabaseManager;
import pl.r3.zlecenie.Zlecenie;

import java.util.*;

public class GuiListener implements Listener {

    private final Zlecenie plugin;
    private final DatabaseManager databaseManager;

    public GuiListener(Zlecenie plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        FileConfiguration config = plugin.getConfig();
        String guiName = config.getString("gui.name");
        if (!event.getView().getTitle().equals(guiName)) {
           return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && !clickedItem.getType().equals(Material.AIR)) {

            //User user = userManager.getUser();
            int playerLevel = databaseManager.getPlayerLevel(player.getUniqueId());
            int highestRewardNumber = databaseManager.getHighestRewardReceived(player.getUniqueId());

            int slot = event.getRawSlot();
            if (slot >= 0 && slot < event.getInventory().getSize()) {
                List<Map<?, ?>> itemsList = config.getMapList("gui.items");
                for (Map<?, ?> itemMap : itemsList) {
                    if (!itemMap.containsKey("slot")) continue;
                    int itemSlot = Integer.parseInt(itemMap.get("slot").toString());
                    if (itemSlot != slot) continue;


                    Object rewardNumberObject = itemMap.get("reward_number");
                    if (rewardNumberObject == null) {
                        plugin.getLogger().warning("Reward number is missing for GUI item.");
                        break;
                    }

                    int rewardNumber = Integer.parseInt(rewardNumberObject.toString());
                    if (highestRewardNumber + 1 == rewardNumber && playerLevel >= rewardNumber) {
                        if (itemMap.containsKey("rewards")) {
                            List<Map<?, ?>> rewardsList = (List<Map<?, ?>>) itemMap.get("rewards");
                            boolean canClaim = true;
                            if (itemMap.containsKey("level_required")) {
                                int requiredLevel = Integer.parseInt(itemMap.get("level_required").toString());
                                if (playerLevel < requiredLevel) {
                                    canClaim = false;
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.too_low_level").replace("%level_required%", String.valueOf(requiredLevel))));
                                }
                            }
                            if (canClaim) {
                                for (Map<?, ?> rewardMap : rewardsList) {
                                    String type = rewardMap.get("type").toString();
                                    int amount = Integer.parseInt(rewardMap.get("amount").toString());
                                    ItemStack rewardItem = new ItemStack(Material.matchMaterial(type), amount);
                                    ItemMeta meta = rewardItem.getItemMeta();
                                    if (rewardMap.containsKey("name")) {
                                        meta.setDisplayName(rewardMap.get("name").toString());
                                    }
                                    if (rewardMap.containsKey("lore")) {
                                        Object loreObj = rewardMap.get("lore");
                                        if (loreObj instanceof List) {
                                            meta.setLore((List<String>) loreObj);
                                        } else {
                                            plugin.getLogger().warning("Invalid lore format for reward item: " + loreObj);
                                        }
                                    }
                                    rewardItem.setItemMeta(meta);
                                    player.getInventory().addItem(rewardItem);
                                    databaseManager.updateHighestRewardReceived(player.getUniqueId(), highestRewardNumber + 1);
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.claim_success")));
                                    GuiManager.openRewardsGUI(player); // Reload GUI after claiming reward
                                }
                            }
                        }
                    } else if (highestRewardNumber + 1 > rewardNumber) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.already_claimed")));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no_requirements")));
                    }
                    break; // ? ? ? ?

                }
            }
        }
    }

    
}