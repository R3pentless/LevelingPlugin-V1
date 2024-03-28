package pl.r3.zlecenie;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GuiManager implements Listener {

    private final Zlecenie plugin;
    private final DatabaseManager databaseManager;

    public GuiManager(Zlecenie plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        FileConfiguration config = plugin.getConfig();
        String guiName = config.getString("gui.name");
        if (event.getView().getTitle().equals(guiName)) {
            event.setCancelled(true);
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && !clickedItem.getType().equals(Material.AIR)) {
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < event.getInventory().getSize()) {
                List<Map<?, ?>> itemsList = config.getMapList("gui.items");
                for (Map<?, ?> itemMap : itemsList) {
                    if (itemMap.containsKey("slot")) {
                        int itemSlot = Integer.parseInt(itemMap.get("slot").toString());
                        if (itemSlot == slot && Boolean.parseBoolean(itemMap.get("reward").toString())) {
                            int playerLevel = databaseManager.getPlayerLevel(player.getUniqueId());
                            int highestRewardNumber = databaseManager.getHighestRewardReceived(player.getUniqueId());
                            Object rewardNumberObject = itemMap.get("reward_number");
                            if (rewardNumberObject != null) {
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
                                                openRewardsGUI(player); // Reload GUI after claiming reward
                                            }
                                        }
                                    }
                                } else if (highestRewardNumber + 1 > rewardNumber) {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.already_claimed")));
                                } else {
                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.no_requirements")));
                                }
                                break;
                            } else {
                                plugin.getLogger().warning("Reward number is missing for GUI item.");
                            }
                        }
                    }
                }
            }
        }
    }

    public void openRewardsGUI(Player player) {
        FileConfiguration config = plugin.getConfig();
        String guiName = config.getString("gui.name");
        int guiSize = config.getInt("gui.size");
        List<Map<?, ?>> itemsList = config.getMapList("gui.items");
        List<Map<?, ?>> decorationItemsList = config.getMapList("gui.items-decoration");

        Inventory inv = plugin.getServer().createInventory(null, guiSize, guiName);

        UUID playerId = player.getUniqueId();
        int highestClaimedReward = databaseManager.getHighestRewardReceived(playerId);

        // Load main items
        for (Map<?, ?> itemMap : itemsList) {
            if (itemMap.containsKey("slot")) {
                try {
                    int slot = Integer.parseInt(itemMap.get("slot").toString());
                    String typeUnclaimed = itemMap.containsKey("type_unclaimed") ? itemMap.get("type_unclaimed").toString() : "STONE";
                    String typeClaimed = itemMap.containsKey("type_claimed") ? itemMap.get("type_claimed").toString() : typeUnclaimed; // Default to original type
                    Material itemType = Material.matchMaterial(highestClaimedReward >= Integer.parseInt(itemMap.get("reward_number").toString()) ? typeClaimed : typeUnclaimed);
                    if (itemType != null) {
                        ItemStack itemStack = new ItemStack(itemType);
                        int amount = itemMap.containsKey("amount") ? Integer.parseInt(itemMap.get("amount").toString()) : 1;
                        itemStack.setAmount(amount);
                        String itemNameUnclaimed = itemMap.containsKey("name_unclaimed") ? itemMap.get("name_unclaimed").toString() : null;
                        String itemNameClaimed = itemMap.containsKey("name_claimed") ? itemMap.get("name_claimed").toString() : itemNameUnclaimed; // Default to unclaimed name
                        String itemName = highestClaimedReward >= Integer.parseInt(itemMap.get("reward_number").toString()) ? itemNameClaimed : itemNameUnclaimed;
                        if (itemName != null) {
                            itemName = ChatColor.translateAlternateColorCodes('&', itemName);
                            ItemMeta meta = itemStack.getItemMeta();
                            meta.setDisplayName(itemName);

                            List<String> lore = new ArrayList<>();
                            if (highestClaimedReward >= Integer.parseInt(itemMap.get("reward_number").toString())) {
                                if (itemMap.containsKey("lore_claimed")) {
                                    String rawLore = itemMap.get("lore_claimed").toString();
                                    String[] lines = rawLore.split("\\|\\|"); // Split on "||"
                                    for (String line : lines) {
                                        lore.add(ChatColor.translateAlternateColorCodes('&', line.trim())); // Trim to remove leading/trailing whitespaces
                                    }
                                }
                            } else {
                                if (itemMap.containsKey("lore_unclaimed")) {
                                    String rawLore = itemMap.get("lore_unclaimed").toString();
                                    String[] lines = rawLore.split("\\|\\|"); // Split on "||"
                                    for (String line : lines) {
                                        lore.add(ChatColor.translateAlternateColorCodes('&', line.trim())); // Trim to remove leading/trailing whitespaces
                                    }
                                }
                            }
                            meta.setLore(lore);
                            itemStack.setItemMeta(meta);
                        }
                        inv.setItem(slot, itemStack);
                    } else {
                        plugin.getLogger().warning("Invalid material type specified for GUI item.");
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid GUI item format: " + itemMap);
                }
            } else {
                plugin.getLogger().warning("Invalid GUI item format: " + itemMap);
            }
        }

        // Load decoration items
        for (Map<?, ?> itemMap : decorationItemsList) {
            if (itemMap.containsKey("slot") && itemMap.containsKey("type")) {
                try {
                    int slot = Integer.parseInt(itemMap.get("slot").toString());
                    Material itemType = Material.matchMaterial(itemMap.get("type").toString());
                    if (itemType != null) {
                        ItemStack itemStack = new ItemStack(itemType);
                        ItemMeta meta = itemStack.getItemMeta();
                        String itemName = ChatColor.translateAlternateColorCodes('&', itemMap.get("name").toString());
                        meta.setDisplayName(itemName);

                        List<String> lore = new ArrayList<>();
                        if (itemMap.containsKey("lore")) {
                            String rawLore = itemMap.get("lore").toString();
                            String[] lines = rawLore.split("\\|\\|");
                            for (String line : lines) {
                                lore.add(ChatColor.translateAlternateColorCodes('&', line.trim()));
                            }
                        }
                        meta.setLore(lore);
                        itemStack.setItemMeta(meta);
                        inv.setItem(slot, itemStack);
                    } else {
                        plugin.getLogger().warning("Invalid material type specified for decoration item.");
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid decoration item format: " + itemMap);
                }
            } else {
                plugin.getLogger().warning("Decoration item missing required parameters (slot, type): " + itemMap);
            }
        }


        player.openInventory(inv);
    }
}
