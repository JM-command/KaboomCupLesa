package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;
import java.util.UUID;

public class TNTPlaceListener implements Listener {
    private final TeamManager teams;
    private final Map<UUID, TeamColor> recentPlaceBy; // stocké dans GameManager et exposé via getter

    public TNTPlaceListener(TeamManager teams, Map<UUID, TeamColor> recentPlaceBy){
        this.teams = teams; this.recentPlaceBy = recentPlaceBy;
    }

    @EventHandler public void onPlace(BlockPlaceEvent e){
        if (e.getBlockPlaced().getType() != Material.TNT) return;
        var t = teams.get(e.getPlayer());
        if (t == null) return;
        // on associe le chunk UUID pour lookup si source manquante
        recentPlaceBy.put(e.getBlockPlaced().getChunk().getChunkKeyUUID(), t);
    }
}
