package ch.jmcommand.kaboomcuplesa.command;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import ch.jmcommand.kaboomcuplesa.team.NametagService;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KaboomCommand implements CommandExecutor, TabCompleter {

    private final KaboomCupLesa plugin;
    private final GameManager game;
    private final TeamManager teams;
    private final NametagService tags;

    public KaboomCommand(KaboomCupLesa plugin,
                         GameManager game,
                         TeamManager teams,
            /* plus de ZoneManager */ Object unusedZones,
                         NametagService tags) {
        this.plugin = plugin;
        this.game = game;
        this.teams = teams;
        this.tags = tags;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("kaboom")) return false;

        if (!sender.hasPermission("kaboom.admin")) {
            sender.sendMessage(plugin.msg("errors.noPermission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "reload": {
                plugin.reloadAll();
                sender.sendMessage(plugin.msg("admin.configReloaded"));
                return true;
            }

            case "start": {
                // optionnel: exiger équipes équilibrées
                if (plugin.getConfig().getBoolean("rules.requireTeamsBalanced", true) && !teams.areBalanced()) {
                    sender.sendMessage(plugin.msg("errors.teamsUnbalanced"));
                    return true;
                }
                if (game.state() != GameState.LOBBY) {
                    sender.sendMessage(plugin.color("&cLe jeu n'est pas en LOBBY."));
                    return true;
                }
                game.start();
                sender.sendMessage(plugin.color("&aStart lancé."));
                return true;
            }

            case "pause": {
                if (game.state() != GameState.RUNNING) {
                    sender.sendMessage(plugin.color("&cLe jeu n'est pas en RUNNING."));
                    return true;
                }
                game.pause();
                sender.sendMessage(plugin.msg("admin.paused"));
                return true;
            }

            case "play": { // alias resume
                if (game.state() != GameState.PAUSED) {
                    sender.sendMessage(plugin.color("&cLe jeu n'est pas en PAUSED."));
                    return true;
                }
                game.resume();
                sender.sendMessage(plugin.msg("admin.resumed"));
                return true;
            }

            case "stop": {
                if (game.state() == GameState.LOBBY) {
                    sender.sendMessage(plugin.color("&eDéjà en LOBBY."));
                    return true;
                }
                game.stop();
                sender.sendMessage(plugin.msg("admin.stopped"));
                return true;
            }

            case "setspawn":
            case "setblue":
            case "setred": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.msg("errors.onlyPlayers"));
                    return true;
                }
                Player p = (Player) sender;
                Location here = p.getLocation().getBlock().getLocation();

                if (sub.equals("setspawn")) {
                    game.setSpawn(here);
                    sender.sendMessage(plugin.color("&aSpawn défini."));
                } else if (sub.equals("setblue")) {
                    game.setBaseBlue(here);
                    sender.sendMessage(plugin.color("&9Base BLUE &adéfinie."));
                } else {
                    game.setBaseRed(here);
                    sender.sendMessage(plugin.color("&cBase RED &adéfinie."));
                }
                return true;
            }

            case "force": {
                if (args.length < 3) {
                    sender.sendMessage(plugin.color("&cUsage: /kaboom force <player> <blue|red>"));
                    return true;
                }
                Player tgt = plugin.getServer().getPlayer(args[1]);
                if (tgt == null) {
                    sender.sendMessage(plugin.msg("errors.playerOffline"));
                    return true;
                }
                TeamColor t = args[2].equalsIgnoreCase("blue") ? TeamColor.BLUE : TeamColor.RED;
                if (teams.isTeamFull(t)) {
                    String txt = plugin.messages().getString("errors.teamFull", "&cÉquipe pleine ({max}).")
                            .replace("{max}", String.valueOf(teams.maxPerTeam()));
                    sender.sendMessage(plugin.color(txt));
                    return true;
                }
                teams.force(tgt, t);
                if (tags != null) tags.apply(tgt, t);
                sender.sendMessage(plugin.msg("admin.forcedTeam", Map.of(
                        "player", tgt.getName(),
                        "team", t.display()
                )));
                return true;
            }

            default: {
                sendHelp(sender);
                return true;
            }
        }
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(plugin.color(plugin.messages().getString("help.header",
                "&eKaboom &7– &fstart, pause, play, stop, setspawn, setblue, setred, force, reload")));
        for (String line : plugin.messages().getStringList("help.lines")) {
            s.sendMessage(plugin.color(line.replace("{freeze}",
                    String.valueOf(plugin.getConfig().getInt("rules.freezeOnStartSeconds", 5)))));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> base = List.of("start", "pause", "play", "stop", "setspawn", "setblue", "setred", "force", "reload");

        if (args.length == 1) {
            String pref = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (String k : base) if (k.startsWith(pref)) out.add(k);
            return out;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("force")) {
            // laisser Bukkit proposer les pseudos -> return null
            return null;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("force")) {
            return List.of("blue", "red");
        }

        return List.of();
    }
}
