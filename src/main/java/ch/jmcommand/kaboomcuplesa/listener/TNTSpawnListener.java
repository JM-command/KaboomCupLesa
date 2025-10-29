package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Map;
import java.util.UUID;

public class TNTSpawnListener implements Listener {
    private final TeamManager teams;
    private final Map<Integer, TeamColor> tntEntityTeam; // entityId -> team
    private final Map<UUID, TeamColor> recentPlaceBy; // chunk key -> team

    public TNTSpawnListener(TeamManager teams, Map<Integer, TeamColor> tntEntityTeam, Map<UUID, TeamColor> recentPlaceBy){
        this.teams = teams; this.tntEntityTeam = tntEntityTeam; this.recentPlaceBy = recentPlaceBy;
    }

    @EventHandler public void onSpawn(EntitySpawnEvent e){
        if (e.getEntityType()!= EntityType.PRIMED_TNT) return;
        TNTPrimed t = (TNTPrimed) e.getEntity();
        TeamColor team = null;
        var src = t.getSource();
        if (src instanceof org.bukkit.entity.Player){
            team = teams.get((org.bukkit.entity.Player) src);
        }
        if (team == null){
            // fallback: chunk récent où TNT a été posée
            team = recentPlaceBy.getOrDefault(e.getLocation().getChunk().getChunkKeyUUID(), null);
        }
        if (team != null){
            tntEntityTeam.put(t.getEntityId(), team);
        }
    }
}
