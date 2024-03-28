package pl.r3.zlecenie;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.r3.zlecenie.gui.GuiListener;
import pl.r3.zlecenie.gui.GuiManager;

public class GuiCommand implements CommandExecutor {

    private final Zlecenie plugin;
    private final DatabaseManager databaseManager;
    private final GuiManager guiManager;

    public GuiCommand(Zlecenie plugin, DatabaseManager databaseManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        guiManager.openRewardsGUI(player);
        return true;
    }
}
