package ch.jmcommand.kaboomcuplesa.command;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.team.NametagService;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import ch.jmcommand.kaboomcuplesa.zone.ZoneManager;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class KaboomCommand implements CommandExecutor, TabCompleter {

    private final KaboomCupLesa plugin;
    private final GameManager game;
    private final TeamManager teams;
    private final ZoneManager zones;
    private final NametagService tags;

    private Location firstPoint = null;
    private ZoneManager.ZoneType pendingZone = null;

    public KaboomCommand(KaboomCupLesa plugin, GameManager game, TeamManager teams, ZoneManager zones, NametagService tags) {
        this.plugin = plugin;
        this.game = game;
        this.teams = teams;
        this.zones = zones;
        this.tags = tags;
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
                if (!teams.areBalanced()) {
                    sender.sendMessage(plugin.msg("errors.teamsUnbalanced"));
                    return true;
                }
                if (!zones.isConfiguredForStart()) {
                    sender.sendMessage(plugin.msg("errors.zonesNotSet"));
                    return true;
                }
                game.start();
                return true;

            case "pause": game.pause(); return true;
            case "resume": game.resume(); return true;
            case "stop": game.stop(); return true;
            case "play": game.play(); return true; // alias de resume

            case "setspawn":
            case "setblue":
            case "setred": {
                if (!(sender instanceof Player)) { sender.sendMessage(plugin.msg("errors.onlyPlayers")); return true; }
                Player p = (Player) sender;
                if (sub.equals("setspawn")) { zones.setSpawn(p.getLocation()); sender.sendMessage(plugin.msg("admin.setSpawn")); }
                else if (sub.equals("setblue")) { zones.setBaseBlue(p.getLocation()); sender.sendMessage(plugin.msg("admin.setBlue")); }
                else { zones.setBaseRed(p.getLocation()); sender.sendMessage(plugin.msg("admin.setRed")); }
                return true;
            }



            

                try {
                    Player p = (Player) sender;

                    // Classes WE par réflexion
                    Class<?> wepCls = we.getClass(); // WorldEditPlugin
                    Class<?> bukkitAdapterCls = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                    Class<?> weWorldCls = Class.forName("com.sk89q.worldedit.world.World");
                    Class<?> blockVector3Cls = Class.forName("com.sk89q.worldedit.math.BlockVector3");

                    // session = wep.getSession(player)
                    Method mGetSession = wepCls.getMethod("getSession", Player.class);
                    Object session = mGetSession.invoke(we, p);

                    // weWorld = BukkitAdapter.adapt(p.getWorld())
                    Method mAdaptWorld = bukkitAdapterCls.getMethod("adapt", org.bukkit.World.class);
                    Object weWorld = mAdaptWorld.invoke(null, p.getWorld());

                    // region = session.getSelection(weWorld)
                    Method mGetSelection = session.getClass().getMethod("getSelection", weWorldCls);
                    Object region = mGetSelection.invoke(session, weWorld);
                    if (region == null) {
                        sender.sendMessage(plugin.color("&cSélection vide. Utilise &e//pos1 &cet &e//pos2"));
                        return true;
                    }

                    // min/max = region.getMinimumPoint() / getMaximumPoint()
                    Method mGetMin = region.getClass().getMethod("getMinimumPoint");
                    Method mGetMax = region.getClass().getMethod("getMaximumPoint");
                    Object min = mGetMin.invoke(region);
                    Object max = mGetMax.invoke(region);

                    // coords = blockVector3.getBlockX/Y/Z()
                    int minX = (int) blockVector3Cls.getMethod("getBlockX").invoke(min);
                    int minY = (int) blockVector3Cls.getMethod("getBlockY").invoke(min);
                    int minZ = (int) blockVector3Cls.getMethod("getBlockZ").invoke(min);
                    int maxX = (int) blockVector3Cls.getMethod("getBlockX").invoke(max);
                    int maxY = (int) blockVector3Cls.getMethod("getBlockY").invoke(max);
                    int maxZ = (int) blockVector3Cls.getMethod("getBlockZ").invoke(max);

                    Location a = new Location(p.getWorld(), minX, minY, minZ);
                    Location b = new Location(p.getWorld(), maxX, maxY, maxZ);
                    zones.setZone(type, a, b);
                    sender.sendMessage(plugin.msg("admin.zoneSaved", Map.of("zone", type.pathKey)));
                } catch (Throwable ex) {
                    sender.sendMessage(plugin.color("&cErreur WorldEdit: " + ex.getClass().getSimpleName()));
                }
                return true;
            }

            case "force": {
                if (args.length < 3) { sender.sendMessage(plugin.msg("errors.invalidArgs")); return true; }
                Player tgt = plugin.getServer().getPlayer(args[1]);
                if (tgt == null) { sender.sendMessage(plugin.msg("errors.playerOffline")); return true; }
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
            return List.of("start", "pause", "resume", "force", "setspawn", "setblue", "setred", "setzone", "setzonewe", "reload");
        if (args.length == 2 && args[0].equalsIgnoreCase("force"))
            return null; // Bukkit proposera les pseudos
        if (args.length == 2 && (args[0].equalsIgnoreCase("setzone") || args[0].equalsIgnoreCase("setzonewe")))
            return List.of("BLUE_ZONE", "RED_ZONE", "WALL_ZONE", "BLUE_SUPPLY", "RED_SUPPLY");
        if (args.length == 3 && args[0].equalsIgnoreCase("force"))
            return List.of("blue", "red");
        return List.of();
    }
}
