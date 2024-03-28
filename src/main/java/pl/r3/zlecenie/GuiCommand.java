package pl.r3.zlecenie;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuiCommand implements CommandExecutor {

    private final Zlecenie plugin;
    private final DatabaseManager databaseManager;

    public GuiCommand(Zlecenie plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        GuiManager guiManager = new GuiManager(plugin, databaseManager);
        guiManager.openRewardsGUI(player);
        return true;
    }
}
