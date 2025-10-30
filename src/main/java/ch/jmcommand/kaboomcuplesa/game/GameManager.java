package ch.jmcommand.kaboomcuplesa.game;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class GameManager {

    private final KaboomCupLesa plugin;
    private final TeamManager teams;

    private GameState state = GameState.LOBBY;
    private boolean freezeActive = false;
    private final MatchTimer timer;

    private Location spawn;
    private Location baseBlue;
    private Location baseRed;

    private final Map<UUID, Integer> lives = new HashMap<>();
    private int defaultLives;

    public GameManager(KaboomCupLesa plugin, TeamManager teams, Object unusedZones) {
        this.plugin = plugin;
        this.teams = teams;
        this.timer = new MatchTimer(plugin, this);

        this.defaultLives = Math.max(1, plugin.getConfig().getInt("rules.livesPerPlayer", 3));

        this.spawn    = loadLocation("spawns.spawn");
        this.baseBlue = loadLocation("spawns.baseBlue");
        this.baseRed  = loadLocation("spawns.baseRed");
    }

    public GameState state() { return state; }

    public boolean isFreezeActive() {
        return freezeActive && state != GameState.RUNNING;
    }

    private void setFreeze(boolean v) {
        this.freezeActive = v;
    }

    public int secondsLeft() { return timer.secondsLeft(); }

    /* ===================== ADMIN FLOW ===================== */

    public void adminStartMatch() {
        if (state != GameState.LOBBY) return;

        if (baseBlue == null || baseRed == null) {
            plugin.warn("Bases non définies. Utilise /kaboom setblue et /kaboom setred");
            return;
        }

        resetAllLivesToDefault();

        int freeze = Math.max(0, plugin.getConfig().getInt("rules.freezeOnStartSeconds", 5));
        setFreeze(true);
        Bukkit.broadcastMessage(plugin.color("&7[&eKaboom&7] &fDépart dans &e" + freeze + "s"));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : teams.online(TeamColor.BLUE)) {
                    safeTeleport(p, baseBlue());
                    p.getInventory().clear();
                    plugin.kits().clearHubItem(p);
                    plugin.kits().giveStartKitIfConfigured(p);
                }
                for (Player p : teams.online(TeamColor.RED)) {
                    safeTeleport(p, baseRed());
                    p.getInventory().clear();
                    plugin.kits().clearHubItem(p);
                    plugin.kits().giveStartKitIfConfigured(p);
                }

                setFreeze(false);
                state = GameState.RUNNING;

                int secs = Math.max(1, plugin.getConfig().getInt("rules.matchDurationSeconds", 1200));
                timer.startSeconds(secs);
                Bukkit.broadcastMessage(plugin.color("&aGo!"));
            }
        }.runTaskLater(plugin, freeze * 20L);
    }

    public void adminPause() {
        if (state != GameState.RUNNING) return;
        state = GameState.PAUSED;
        setFreeze(true);
        timer.pause();
        Bukkit.broadcastMessage(plugin.color("&7[&eKaboom&7] &fPartie &ePAUSE"));
    }

    public void adminResume() {
        if (state != GameState.PAUSED) return;
        state = GameState.RUNNING;
        setFreeze(false);
        timer.resume();
        Bukkit.broadcastMessage(plugin.color("&7[&eKaboom&7] &fPartie &aREPRISE"));
    }

    public void adminStop() {
        timer.stop();
        state = GameState.LOBBY;
        setFreeze(false);
        lives.clear();

        if (spawn != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                safeTeleport(p, spawn());
                plugin.kits().giveHubItem(p);
            }
        }
        Bukkit.broadcastMessage(plugin.color("&7[&eKaboom&7] &fPartie terminée."));
    }

    /* ===================== ANTI-VOID + KILL MESSAGE ===================== */

    public void handleVoidFall(Player p, TeamColor team) {
        if (state != GameState.RUNNING) return;

        // On cherche si quelqu’un l’a frappé juste avant
        UUID killerId = plugin.damageTracker() != null
                ? plugin.damageTracker().getLastDamager(p.getUniqueId())
                : null;

        Player killer = (killerId == null ? null : plugin.getServer().getPlayer(killerId));
        TeamColor killerTeam = (killer == null ? null : teams.get(killer));

        int left = (lives.getOrDefault(p.getUniqueId(), defaultLives)) - 1;
        lives.put(p.getUniqueId(), left);

        // Message de kill AVANT TP
        if (killer != null && killerTeam != null && killerTeam != team) {
            // killer ≠ victime, et pas même équipe
            String msg = plugin.msg("kills.voidPushed", Map.of(
                    "killer", killer.getName(),
                    "victim", p.getName()
            ));
            Bukkit.broadcastMessage(msg);
        } else {
            // chute solo
            String msg = plugin.msg("kills.voidSolo", Map.of(
                    "victim", p.getName()
            ));
            Bukkit.broadcastMessage(msg);
        }

        if (left > 0) {
            Location base = (team == TeamColor.BLUE ? baseBlue() : baseRed());
            if (base != null) safeTeleport(p, base);
            p.setFallDistance(0f); // évite de reperdre des PV derrière
            p.setHealth(Math.max(1.0, p.getHealth())); // petit safety
            p.sendMessage(plugin.color("&cTu es tombé ! &7Vies restantes: &e" + left));
        } else {
            p.sendMessage(plugin.color("&cPlus de vies. Tu es éliminé."));
            if (spawn != null) safeTeleport(p, spawn());
            teams.setSpectator(p);
        }

        checkWinCondition();
    }

    /* ===================== SCOREBOARD HELPERS ===================== */

    public int teamSizeBlue() { return teams.size(TeamColor.BLUE); }
    public int teamSizeRed()  { return teams.size(TeamColor.RED);  }

    public TeamColor teamOf(Player p) { return teams.get(p); }

    public String livesStringBlue() { return livesStringFor(TeamColor.BLUE); }
    public String livesStringRed()  { return livesStringFor(TeamColor.RED);  }

    private String livesStringFor(TeamColor team) {
        var ps = teams.online(team);
        if (ps.isEmpty()) return "-";
        return ps.stream()
                .map(p -> p.getName() + ":" + lives.getOrDefault(p.getUniqueId(), defaultLives))
                .collect(Collectors.joining(" "));
    }

    /* ===================== SPAWN / BASES ===================== */

    public void setSpawn(Location l) {
        this.spawn = (l == null ? null : l.clone());
        saveLocation("spawns.spawn", this.spawn);
    }

    public void setBaseBlue(Location l) {
        this.baseBlue = (l == null ? null : l.clone());
        saveLocation("spawns.baseBlue", this.baseBlue);
    }

    public void setBaseRed(Location l) {
        this.baseRed = (l == null ? null : l.clone());
        saveLocation("spawns.baseRed", this.baseRed);
    }

    public Location spawn()    { return spawn == null ? null : spawn.clone(); }
    public Location baseBlue() { return baseBlue == null ? null : baseBlue.clone(); }
    public Location baseRed()  { return baseRed == null ? null : baseRed.clone(); }

    /* ===================== VICTOIRE ===================== */

    private void checkWinCondition() {
        boolean blueAlive = teams.online(TeamColor.BLUE).stream()
                .anyMatch(p -> lives.getOrDefault(p.getUniqueId(), defaultLives) > 0);
        boolean redAlive = teams.online(TeamColor.RED).stream()
                .anyMatch(p -> lives.getOrDefault(p.getUniqueId(), defaultLives) > 0);

        if (state == GameState.RUNNING && (!blueAlive || !redAlive)) {
            String msg = !blueAlive && !redAlive
                    ? "&eÉgalité !"
                    : (!blueAlive ? "&cRouge gagne !" : "&9Bleu gagne !");
            Bukkit.broadcastMessage(plugin.color("&7[&eKaboom&7] " + msg));
            adminStop();
        }
    }

    private void resetAllLivesToDefault() {
        lives.clear();
        for (Player p : Bukkit.getOnlinePlayers()) {
            lives.put(p.getUniqueId(), defaultLives);
        }
    }

    private void safeTeleport(Player p, Location l) {
        if (l == null) return;
        World w = l.getWorld();
        if (w == null) return;
        p.teleport(l);
    }

    /* ===================== CONFIG I/O ===================== */

    private void saveLocation(String path, Location loc) {
        var cfg = plugin.getConfig();
        if (loc == null) {
            cfg.set(path, null);
        } else {
            cfg.set(path + ".world", loc.getWorld().getName());
            cfg.set(path + ".x", loc.getX());
            cfg.set(path + ".y", loc.getY());
            cfg.set(path + ".z", loc.getZ());
            cfg.set(path + ".yaw", loc.getYaw());
            cfg.set(path + ".pitch", loc.getPitch());
        }
        plugin.saveConfig();
    }

    private Location loadLocation(String path) {
        var cfg = plugin.getConfig();
        String w = cfg.getString(path + ".world", null);
        if (w == null) return null;
        World world = Bukkit.getWorld(w);
        if (world == null) return null;
        double x = cfg.getDouble(path + ".x");
        double y = cfg.getDouble(path + ".y");
        double z = cfg.getDouble(path + ".z");
        float yaw = (float) cfg.getDouble(path + ".yaw", 0.0);
        float pitch = (float) cfg.getDouble(path + ".pitch", 0.0);
        return new Location(world, x, y, z, yaw, pitch);
    }

    /* ===================== MATCH TIMER INTERNE ===================== */

    private static class MatchTimer {
        private final KaboomCupLesa plugin;
        private final GameManager game;
        private org.bukkit.scheduler.BukkitRunnable task;
        private int secondsLeft;
        private boolean running = false;

        public MatchTimer(KaboomCupLesa plugin, GameManager game) {
            this.plugin = plugin;
            this.game = game;
        }

        public void startSeconds(int secs) {
            stop();
            this.secondsLeft = secs;
            this.running = true;
            this.task = new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (!running) { cancel(); return; }
                    if (game.state != GameState.RUNNING) return;
                    secondsLeft = Math.max(0, secondsLeft - 1);
                    if (secondsLeft <= 0) {
                        Bukkit.broadcastMessage(plugin.color("&7[&eKaboom&7] &fTemps écoulé."));
                        game.adminStop();
                        cancel();
                    }
                }
            };
            this.task.runTaskTimer(plugin, 20L, 20L);
        }

        public void pause() { this.running = false; }
        public void resume() { this.running = true; }
        public void stop() {
            this.running = false;
            if (this.task != null) {
                try { this.task.cancel(); } catch (Throwable ignored) {}
            }
            this.task = null;
            this.secondsLeft = 0;
        }
        public int secondsLeft() { return Math.max(0, secondsLeft); }
    }

    public TeamColor leadingTeamColor() {
        // si t'as pas de league configurée, on évite le NPE
        if (plugin.league() == null) return null;

        int bluePts = plugin.league().getPoints(TeamColor.BLUE);
        int redPts  = plugin.league().getPoints(TeamColor.RED);

        if (bluePts > redPts) return TeamColor.BLUE;
        if (redPts > bluePts) return TeamColor.RED;
        return null; // égalité
    }

    public String leadingTeamLabel() {
        TeamColor tc = leadingTeamColor();
        if (tc == null) {
            return "&7Aucun (égalité)";
        }
        return (tc == TeamColor.BLUE) ? "&9Blue" : "&cRed";
    }

    public String leadingTeamPointsLine() {
        if (plugin.league() == null) {
            return "&7Leader: &f-";
        }

        int bluePts = plugin.league().getPoints(TeamColor.BLUE);
        int redPts  = plugin.league().getPoints(TeamColor.RED);

        if (bluePts > redPts) {
            return "&7Leader: &9Blue &7(" + bluePts + " pts)";
        } else if (redPts > bluePts) {
            return "&7Leader: &cRed &7(" + redPts + " pts)";
        } else {
            return "&7Leader: &fÉgalité (" + bluePts + " - " + redPts + ")";
        }
    }

}
