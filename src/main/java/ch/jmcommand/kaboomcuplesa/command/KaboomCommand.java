package ch.jmcommand.kaboomcuplesa.command;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.kit.KitService;
import ch.jmcommand.kaboomcuplesa.team.NametagService;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class KaboomCommand implements CommandExecutor, TabCompleter {

    private final KaboomCupLesa plugin;
    private final GameManager game;
    private final TeamManager teams;
    private final NametagService tags;
    private final KitService kits;

    public KaboomCommand(KaboomCupLesa plugin,
                         GameManager game,
                         TeamManager teams,
                         NametagService tags,
                         KitService kits) {
        this.plugin = plugin;
        this.game = game;
        this.teams = teams;
        this.tags = tags;
        this.kits = kits;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("kaboom.admin")) {
            sender.sendMessage(plugin.msg("errors.noPermission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "reload":
                plugin.reloadAll();
                sender.sendMessage(plugin.msg("admin.configReloaded"));
                return true;

            case "start":
                if (plugin.getConfig().getBoolean("rules.requireTeamsBalanced", true) && !teams.areBalanced()) {
                    sender.sendMessage(plugin.msg("errors.teamsUnbalanced"));
                    return true;
                }
                game.adminStartMatch();
                return true;

            case "pause":
                game.adminPause();
                return true;

            case "play": // reprise
            case "resume":
                game.adminResume();
                return true;

            case "stop":
                game.adminStop();
                return true;

            case "setspawn":
            case "setblue":
            case "setred": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.msg("errors.onlyPlayers"));
                    return true;
                }
                Player p = (Player) sender;
                Location loc = p.getLocation();
                switch (sub) {
                    case "setspawn":
                        game.setSpawn(loc);
                        sender.sendMessage(plugin.msg("admin.setSpawn"));
                        break;
                    case "setblue":
                        game.setBaseBlue(loc);
                        sender.sendMessage(plugin.msg("admin.setBlue"));
                        break;
                    case "setred":
                        game.setBaseRed(loc);
                        sender.sendMessage(plugin.msg("admin.setRed"));
                        break;
                }
                return true;
            }

            case "force": {
                if (args.length < 3) {
                    sender.sendMessage(plugin.msg("errors.invalidArgs"));
                    return true;
                }
                Player tgt = plugin.getServer().getPlayer(args[1]);
                if (tgt == null) {
                    sender.sendMessage(plugin.msg("errors.playerOffline"));
                    return true;
                }
                TeamColor t = args[2].equalsIgnoreCase("blue") ? TeamColor.BLUE : TeamColor.RED;
                if (teams.isTeamFull(t)) {
                    sender.sendMessage(plugin.color(plugin.messages().getString("errors.teamFull")
                            .replace("{max}", String.valueOf(teams.maxPerTeam()))));
                    return true;
                }
                teams.force(tgt, t);
                tags.apply(tgt, t);
                sender.sendMessage(plugin.msg("admin.forcedTeam",
                        Map.of("player", tgt.getName(), "team", t.display())));
                return true;
            }

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(plugin.color(plugin.messages().getString("help.header")));
        for (String line : plugin.messages().getStringList("help.lines")) {
            s.sendMessage(plugin.color(line.replace("{freeze}",
                    String.valueOf(plugin.getConfig().getInt("rules.freezeOnStartSeconds", 5)))));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return List.of("start", "pause", "play", "stop", "force", "setspawn", "setblue", "setred", "reload");
        if (args.length == 2 && args[0].equalsIgnoreCase("force"))
            return null; // pseudos
        if (args.length == 3 && args[0].equalsIgnoreCase("force"))
            return List.of("blue", "red");
        return List.of();
    }
}
