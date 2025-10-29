package ch.jmcommand.kaboomcuplesa.command;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.kit.KitService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KitCommand implements CommandExecutor {

    private final KaboomCupLesa plugin;
    private final KitService kits;

    public KitCommand(KaboomCupLesa plugin, KitService kits){
        this.plugin = plugin; this.kits = kits;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("Players only.");
            return true;
        }
        Player p = (Player) sender;
        if (args.length < 1){
            p.sendMessage(plugin.color("&7Usage: &f/kit &7<simple|tnt|pioche|redstone|blocks|water>"));
            return true;
        }
        String key = args[0].toLowerCase();
        kits.giveKit(p, key);
        return true;
    }
}
