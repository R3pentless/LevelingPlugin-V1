package pl.r3.zlecenie;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigCreator {

    public static void createConfigFile(JavaPlugin plugin) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File configFile = new File(dataFolder, "config.yml");
        File databaseFile = new File(dataFolder, "database.yml");

        // Check if files already exist
        if (configFile.exists() && databaseFile.exists()) {
            System.out.println("[/] config.yml");
            System.out.println("[/] database.yml");
            return;
        }

        try {
            FileWriter writer = new FileWriter(configFile);

            // Write the base configuration
            writer.write("gui:\n");
            writer.write("  name: \"nazwa\"\n");
            writer.write("  size: 27\n");
            writer.write("  items:\n");
            writer.write("    - slot: 0\n");
            writer.write("      type_unclaimed: chest_minecart\n");
            writer.write("      type_claimed: minecart\n");
            writer.write("      amount: 1\n");
            writer.write("      name_unclaimed: \"&5Nagroda #1\"\n");
            writer.write("      lore_unclaimed: \"&7This is the first reward||&7linijka2\"\n");
            writer.write("      name_claimed: \"&5Nagroda #1 (Claimed)\"\n");
            writer.write("      lore_claimed: \"&7This reward has already been claimed\"\n");
            writer.write("      reward: true\n");
            writer.write("      reward_number: 1\n");
            writer.write("      level_required: 5\n");
            writer.write("      rewards:\n");
            writer.write("        - reward: 1\n");
            writer.write("          type: stone\n");
            writer.write("          amount: 1\n");
            writer.write("          name: \"Cool Reward\"\n");
            writer.write("          lore: \"&bThis is a cool reward\"\n");
            writer.write("  items-decoration:\n");
            writer.write("    - slot: 10\n");
            writer.write("      type: RED_STAINED_GLASS_PANE\n");
            writer.write("      amount: 1\n");
            writer.write("      name: \"&7Name\"\n");
            writer.write("      lore: \"&7decoration||&7line2\"\n");
            writer.write("\n");
            writer.write("messages:\n");
            writer.write("  no_requirements: \"&7Nie spelniasz wymogow aby odebrac ta nagrode!\"\n");
            writer.write("  already_claimed: \"&cJuz odebrales ta nagrode!\"\n");
            writer.write("  claim_success: \"&aOdebrales nagrode!\"\n");
            writer.write("messages_level:\n");
            writer.write("  received_exp: \"+%exp% [%currentexp%/%requiredexp%] (%percent%%)\"\n");
            writer.write("  reached_level: \"&aCongratulations! You've reached level %level%.\"\n");
            writer.write("  too_low_level: \"&cYou need to be at least level %level_required% to claim this reward!\"\n");
            writer.write("exp:\n");
            writer.write("  activities:\n");
            writer.write("    entities:\n");
            writer.write("      ZOMBIE: 1\n");
            writer.write("      SKELETON: 2\n");
            writer.write("      SPIDER: 1\n");
            writer.write("    blocks:\n");
            writer.write("      DIAMOND_ORE: 3\n");
            writer.write("      COAL_ORE: 1\n");
            writer.write("levels:\n");
            writer.write("  experience_required:\n");
            writer.write("    1: 100\n");
            writer.write("    2: 200\n");
            writer.write("    3: 300\n");
            writer.write("    4: 400\n");
            writer.write("    5: 500\n");
            writer.write("  max_level: 5\n");

            writer.close();
            System.out.println("[+] config.yml");
        } catch (IOException e) {
            System.out.println("Config nie zostal wygenerowany!");
            e.printStackTrace();
        }
    }
}

