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

            case "play":
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
                sender.sendMessage(plugin.msg("admin.forcedTeam", Map.of("player", tgt.getName(), "team", t.display())));
                return true;
            }

            case "league":
                return handleLeague(sender, args);

            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleLeague(CommandSender sender, String[] args) {
        // /kaboom league
        if (args.length == 1) {
            int week = plugin.league().getWeek();
            int blue = plugin.league().getPoints(TeamColor.BLUE);
            int red = plugin.league().getPoints(TeamColor.RED);
            sender.sendMessage(plugin.msg("league.header"));
            sender.sendMessage(plugin.msg("league.currentWeek", Map.of("week", String.valueOf(week))));
            sender.sendMessage(plugin.msg("league.pointsBlue", Map.of("points", String.valueOf(blue))));
            sender.sendMessage(plugin.msg("league.pointsRed", Map.of("points", String.valueOf(red))));
            sender.sendMessage(plugin.msg("league.usage"));
            return true;
        }

        // /kaboom league week <n>
        if (args.length >= 3 && args[1].equalsIgnoreCase("week")) {
            int n;
            try {
                n = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(plugin.msg("errors.invalidArgs"));
                return true;
            }
            plugin.league().setWeek(n);
            sender.sendMessage(plugin.msg("league.setWeek", Map.of("week", String.valueOf(n))));
            return true;
        }

        // /kaboom league add <blue|red> <points>
        if (args.length >= 4 && args[1].equalsIgnoreCase("add")) {
            TeamColor t = args[2].equalsIgnoreCase("blue") ? TeamColor.BLUE : TeamColor.RED;
            int pts;
            try {
                pts = Integer.parseInt(args[3]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(plugin.msg("errors.invalidArgs"));
                return true;
            }
            plugin.league().addPoint(t, pts);
            sender.sendMessage(plugin.msg("league.addPoints", Map.of(
                    "team", t.display(),
                    "points", String.valueOf(pts),
                    "total", String.valueOf(plugin.league().getPoints(t))
            )));
            return true;
        }

        sender.sendMessage(plugin.msg("errors.invalidArgs"));
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(plugin.color(plugin.messages().getString("help.header")));
        for (String line : plugin.messages().getStringList("help.lines")) {
            s.sendMessage(plugin.color(line.replace("{freeze}",
                    String.valueOf(plugin.getConfig().getInt("rules.freezeOnStartSeconds", 5)))));
        }
        // on ajoute une ligne league
        s.sendMessage(plugin.msg("help.leagueExtra"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return List.of("start", "pause", "play", "stop", "force", "setspawn", "setblue", "setred", "reload", "league");
        if (args.length == 2 && args[0].equalsIgnoreCase("force"))
            return null; // pseudos
        if (args.length == 3 && args[0].equalsIgnoreCase("force"))
            return List.of("blue", "red");

        // /kaboom league ...
        if (args.length == 2 && args[0].equalsIgnoreCase("league")) {
            return List.of("week", "add");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("league") && args[1].equalsIgnoreCase("add")) {
            return List.of("blue", "red");
        }
        return List.of();
    }
}
