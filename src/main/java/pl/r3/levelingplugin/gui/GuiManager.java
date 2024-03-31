package pl.r3.levelingplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.r3.levelingplugin.LevelingPlugin;
import pl.r3.levelingplugin.utills.ColorFixer;
import pl.r3.levelingplugin.user.User;
import pl.r3.levelingplugin.user.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GuiManager {
    private static FileConfiguration config;
    private static ColorFixer colorFixer;
    private static LevelingPlugin plugin;
    private static UserManager userManager;

    public GuiManager(LevelingPlugin levelingPlugin, FileConfiguration config, UserManager userManager) {
        this.plugin = levelingPlugin;
        this.config = config;
        this.colorFixer = new ColorFixer();
        this.userManager = userManager;
    }

    public static void openRewardsGUI(Player p) {
        User user = userManager.getUserData(p.getUniqueId());
        String guiName = config.getString("gui.name");
        int guiSize = config.getInt("gui.size");
        List<Map<?, ?>> itemsList = config.getMapList("gui.items");
        List<Map<?, ?>> decorationItemsList = config.getMapList("gui.items-decoration");

        Inventory inv = Bukkit.createInventory(null, guiSize, colorFixer.addColors(guiName));

        int highestClaimedReward = (user != null) ? user.getHighestReward() : 0;

        for (Map<?, ?> itemMap : itemsList) {
            if (itemMap.containsKey("slot")) {
                try {
                    int slot = Integer.parseInt(itemMap.get("slot").toString());

                    int rewardNumber = Integer.parseInt(itemMap.get("reward_number").toString());
                    boolean isClaimed = highestClaimedReward >= rewardNumber;

                    String type = itemMap.containsKey(isClaimed ? "type_claimed" : "type_unclaimed") ? itemMap.get(isClaimed ? "type_claimed" : "type_unclaimed").toString() : "STONE";

                    Material itemType = Material.matchMaterial(type);
                    if (itemType != null) {
                        ItemStack itemStack = new ItemStack(itemType);

                        int amount = itemMap.containsKey("amount") ? Integer.parseInt(itemMap.get("amount").toString()) : 1;
                        itemStack.setAmount(amount);
                        String itemName = isClaimed ? (itemMap.containsKey("name_claimed") ? itemMap.get("name_claimed").toString() : null) : (itemMap.containsKey("name_unclaimed") ? itemMap.get("name_unclaimed").toString() : null);
                        if (itemName != null) {
                            itemName = ChatColor.translateAlternateColorCodes('&', itemName);
                            ItemMeta meta = itemStack.getItemMeta();
                            meta.setDisplayName(itemName);

                            List<String> lore = new ArrayList<>();
                            String loreKey = isClaimed ? "lore_claimed" : "lore_unclaimed";
                            if (itemMap.containsKey(loreKey)) {
                                String rawLore = itemMap.get(loreKey).toString();
                                String[] lines = rawLore.split("\\|\\|"); // Split on "||"
                                for (String line : lines) {
                                    lore.add(ChatColor.translateAlternateColorCodes('&', line.trim())); // Trim to remove leading/trailing whitespaces
                                }
                            }
                            meta.setLore(lore);
                            itemStack.setItemMeta(meta);
                        }

                        inv.setItem(slot, itemStack);
                    } else {
                        plugin.getLogger().warning("Invalid material type specified for item at slot " + slot + ".");
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid GUI item format: " + itemMap);
                }
            } else {
                plugin.getLogger().warning("Invalid GUI item format: " + itemMap);
            }
        }

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

        p.openInventory(inv);
    }
}
