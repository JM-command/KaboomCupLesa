package ch.jmcommand.kaboomcuplesa.game;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import ch.jmcommand.kaboomcuplesa.zone.ZoneManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class DeathService {
    private final KaboomCupLesa plugin;
    private final TeamManager teams;
    private final ZoneManager zones;
    private final GameManager game;

    public DeathService(KaboomCupLesa p, TeamManager t, ZoneManager z, GameManager g){
        this.plugin = p; this.teams = t; this.zones = z; this.game = g;
    }

    public void handleDeath(Player p, Map<UUID,Integer> livesMap){
        int left = livesMap.getOrDefault(p.getUniqueId(), 0) - 1;
        livesMap.put(p.getUniqueId(), left);
        TeamColor tc = teams.get(p);

        if (left > 0){
            p.sendMessage(plugin.msg("announce.livesLeft", Map.of("lives", String.valueOf(left))));
            plugin.getServer().getScheduler().runTask(plugin, ()->{
                p.spigot().respawn();
                p.teleport(tc==TeamColor.BLUE ? zones.baseBlue() : zones.baseRed());
            });
        } else {
            p.sendMessage(plugin.msg("announce.outOfLives"));
            teams.setSpectator(p);
            plugin.getServer().getScheduler().runTask(plugin, ()->{
                p.spigot().respawn();
                p.teleport(zones.spawn());
            });
        }

        game.addDeath(tc);
    }
}
