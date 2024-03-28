package pl.r3.zlecenie.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.r3.zlecenie.Zlecenie;
import pl.r3.zlecenie.utills.ColorFixer;
import pl.r3.zlecenie.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiManager {
    private static FileConfiguration config;
    private static User user;
    private static ColorFixer colorFixer;
    private static Zlecenie plugin;

    public GuiManager(Zlecenie plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public static void openRewardsGUI(Player player) {
        String guiName = config.getString("gui.name");
        int guiSize = config.getInt("gui.size");
        List<Map<?, ?>> itemsList = config.getMapList("gui.items");
        List<Map<?, ?>> decorationItemsList = config.getMapList("gui.items-decoration");

        Inventory inv = Bukkit.createInventory(null, guiSize, colorFixer.addColors(guiName));

        UUID playerId = player.getUniqueId();
        int highestClaimedReward = user.getHighestRewardReceived();

        // Load main items
        for (Map<?, ?> itemMap : itemsList) {
            if (itemMap.containsKey("slot")) {
                try {

                    int slot = Integer.parseInt(itemMap.get("slot").toString());
                    ItemStack itemStack = getItemFromConfig(itemMap, highestClaimedReward);
                    if(itemStack == null)
                        continue;

                    inv.setItem(slot, itemStack);

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

    public static ItemStack getItemFromConfig(Map<?, ?> itemMap, int highestClaimedReward) {
        String typeUnclaimed = itemMap.containsKey("type_unclaimed") ? itemMap.get("type_unclaimed").toString() : "STONE";
        String typeClaimed = itemMap.containsKey("type_claimed") ? itemMap.get("type_claimed").toString() : typeUnclaimed; // Default to original type
        Material itemType = Material.matchMaterial(highestClaimedReward >= Integer.parseInt(itemMap.get("reward_number").toString()) ? typeClaimed : typeUnclaimed);
        ItemStack itemStack = null;
        if (itemType != null) {
            itemStack = new ItemStack(itemType);

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
        }
        return itemStack;
    }
}
