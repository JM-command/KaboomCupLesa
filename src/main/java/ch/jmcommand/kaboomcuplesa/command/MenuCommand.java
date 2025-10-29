package ch.jmcommand.kaboomcuplesa.command;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.ui.TeamMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ch.jmcommand.kaboomcuplesa.game.GameState;

public class MenuCommand implements CommandExecutor {

    private final TeamMenu menu;
    private KaboomCupLesa plugin;

    public MenuCommand(TeamMenu m){ this.menu = m; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("Players only.");
            return true;
        }
        Player p = (Player) sender;
        if (plugin.getGameState() != GameState.LOBBY){
            p.sendMessage(plugin.color("&cLe jeu est en cours, menu désactivé."));
            return true;
        }
        menu.open(p);
        menu.open(p);
        return true;
    }
}
