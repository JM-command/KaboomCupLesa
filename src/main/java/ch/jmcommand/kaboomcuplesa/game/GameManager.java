package ch.jmcommand.kaboomcuplesa.game;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import ch.jmcommand.kaboomcuplesa.zone.ZoneManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final KaboomCupLesa plugin;
    private final TeamManager teams;
    private final ZoneManager zones;
    private final MatchTimer timer;
    private final DeathService deaths;

    private GameState state = GameState.LOBBY;

    private final Map<UUID,Integer> lives = new HashMap<>();
    private final Map<TeamColor,Integer> deathCount = new EnumMap<>(TeamColor.class);
    private final Map<TeamColor,Integer> blocksBroken = new EnumMap<>(TeamColor.class);

    public GameManager(KaboomCupLesa p, TeamManager t, ZoneManager z){
        this.plugin = p; this.teams = t; this.zones = z;
        this.timer = new MatchTimer(p);
        this.deaths = new DeathService(p, t, z, this);
        resetStats();
    }

    public void resetStats(){
        lives.clear();
        for (Player pl : Bukkit.getOnlinePlayers()){
            lives.put(pl.getUniqueId(), plugin.getConfig().getInt("rules.livesPerPlayer",3));
        }
        deathCount.put(TeamColor.BLUE, 0);
        deathCount.put(TeamColor.RED, 0);
        blocksBroken.put(TeamColor.BLUE, 0);
        blocksBroken.put(TeamColor.RED, 0);
    }

    public GameState state(){ return state; }
    private boolean freezeActive = false;
    private final Map<Integer, TeamColor> tntEntityTeam = new HashMap<>();
    private final Map<java.util.UUID, TeamColor> recentPlaceBy = new HashMap<>();

    public boolean isFreezeActive(){ return freezeActive; }
    public Map<Integer, TeamColor> tntEntityTeam(){ return tntEntityTeam; }
    public Map<java.util.UUID, TeamColor> recentPlaceBy(){ return recentPlaceBy; }

    public int teamSizeBlue(){ return teams.size(TeamColor.BLUE); }
    public int teamSizeRed(){ return teams.size(TeamColor.RED); }

    // start()
    public void start(){
        if (state != GameState.LOBBY) return;

        // freeze countdown
        int freeze = plugin.getConfig().getInt("rules.freezeOnStartSeconds", 5);
        freezeActive = true;
        org.bukkit.Bukkit.broadcastMessage(plugin.msg("admin.startCountdown", java.util.Map.of("seconds", String.valueOf(freeze))));

        // TP bases + clear + kits + nametags déjà gérés ailleurs
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, ()->{
            for (var p : teams.online(TeamColor.BLUE)) {
                p.teleport(plugin.baseBlue());
                p.getInventory().clear();
                plugin.getJoinItemListener().clearJoinItem(p);
                plugin.getKitManager().giveStartKitIfConfigured(p);
            }
            for (var p : teams.online(TeamColor.RED)) {
                p.teleport(plugin.baseRed());
                p.getInventory().clear();
                plugin.getJoinItemListener().clearJoinItem(p);
                plugin.getKitManager().giveStartKitIfConfigured(p);
            }

            freezeActive = false;
            state = GameState.RUNNING;
            int secs = plugin.getConfig().getInt("rules.matchDurationSeconds", 1200);
            timer.startSeconds(secs);
            org.bukkit.Bukkit.broadcastMessage(plugin.msg("admin.go"));
        }, freeze * 20L);
    }

    public void stop(){
        timer.stop();
        state = GameState.LOBBY;
        // TP spawn + clear stats + donner item hub
        var sp = plugin.spawn();
        if (sp != null){
            for (var p : org.bukkit.Bukkit.getOnlinePlayers()){
                p.teleport(sp);
                plugin.getJoinItemListener().giveJoinItem(p);
            }
        }
        resetStats();
    }

    public void play(){ // alias de resume
        resume();
    }

    public void voidFallPunish(org.bukkit.entity.Player p, TeamColor t){
        // -1 vie comme deathService mais sans écran
        int left = lives.getOrDefault(p.getUniqueId(), 0) - 1;
        lives.put(p.getUniqueId(), left);
        if (left > 0){
            p.teleport(t==TeamColor.BLUE ? plugin.baseBlue() : plugin.baseRed());
            p.sendMessage(plugin.msg("announce.livesLeft", java.util.Map.of("lives", String.valueOf(left))));
        } else {
            teams.setSpectator(p);
            p.teleport(plugin.spawn());
            p.sendMessage(plugin.msg("announce.outOfLives"));
        }
        addDeath(t);
        // élimination check...
    }

    public void pause(){
        if (state != GameState.RUNNING) return;
        state = GameState.PAUSED;
        Bukkit.broadcastMessage(plugin.msg("admin.paused"));
    }

    public void resume(){
        if (state != GameState.PAUSED) return;
        state = GameState.RUNNING;
        Bukkit.broadcastMessage(plugin.msg("admin.resumed"));
    }

    public void stopToLobby(){
        timer.stop();
        state = GameState.LOBBY;
        // TP tout le monde au spawn si défini
        Location sp = zones.spawn();
        if (sp != null){
            for (Player p : Bukkit.getOnlinePlayers()) p.teleport(sp);
        }
        resetStats();
    }

    public void onPlayerDeath(Player p){
        if (state != GameState.RUNNING) return;
        if (!teams.isPlaying(p)) return;
        deaths.handleDeath(p, lives);

        // Check élimination d'équipe
        if (isTeamEliminated(TeamColor.BLUE)){
            Bukkit.broadcastMessage(plugin.msg("announce.redWinsElim"));
            stopToLobby();
        } else if (isTeamEliminated(TeamColor.RED)){
            Bukkit.broadcastMessage(plugin.msg("announce.blueWinsElim"));
            stopToLobby();
        }
    }

    private boolean isTeamEliminated(TeamColor t){
        for (Player pl : teams.online(t)){
            if (lives.getOrDefault(pl.getUniqueId(),0) > 0) return false;
        }
        return true;
    }

    public void addBrokenBlock(TeamColor breaker, Location loc){
        if (state == GameState.LOBBY) return;
        if (zones.wallZone()!=null && zones.wallZone().contains(loc)) return;

        if (zones.redZone()!=null && zones.redZone().contains(loc) && breaker == TeamColor.BLUE){
            blocksBroken.put(TeamColor.BLUE, blocksBroken.get(TeamColor.BLUE)+1);
        } else if (zones.blueZone()!=null && zones.blueZone().contains(loc) && breaker == TeamColor.RED){
            blocksBroken.put(TeamColor.RED, blocksBroken.get(TeamColor.RED)+1);
        }
    }

    public void addDeath(TeamColor team){
        deathCount.put(team, deathCount.get(team)+1);
    }

    public int deaths(TeamColor t){ return deathCount.get(t); }
    public int blocks(TeamColor t){ return blocksBroken.get(t); }
    public int livesLeft(UUID id){ return lives.getOrDefault(id, 0); }
    public int secondsLeft(){ return timer.secondsLeft(); }

    // Timeout winner
    public TeamColor winnerOnTimeout(){
        int bd = deaths(TeamColor.BLUE), rd = deaths(TeamColor.RED);
        if (bd != rd) return bd < rd ? TeamColor.BLUE : TeamColor.RED;
        int bb = blocks(TeamColor.BLUE), rb = blocks(TeamColor.RED);
        return bb >= rb ? TeamColor.BLUE : TeamColor.RED;
    }

    public void tickTimeoutCheck(){
        if (state != GameState.RUNNING) return;
        if (timer.isOver()){
            TeamColor win = winnerOnTimeout();
            Bukkit.broadcastMessage(plugin.msg("announce.timeoutWinner", Map.of("team", win.display())));
            stopToLobby();
        }
    }
}
