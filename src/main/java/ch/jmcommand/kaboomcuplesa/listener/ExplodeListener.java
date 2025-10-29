package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Map;

public class ExplodeListener implements Listener {

    private final GameManager game;
    private final Map<Integer, TeamColor> tntEntityTeam;
    private final int middleX;

    public ExplodeListener(GameManager game, Map<Integer, TeamColor> tntEntityTeam, int middleX){
        this.game = game; this.tntEntityTeam = tntEntityTeam; this.middleX = middleX;
    }

    @EventHandler public void onExplode(EntityExplodeEvent e){
        var team = tntEntityTeam.remove(e.getEntity().getEntityId());
        if (team == null) return;

        // On score UNIQUEMENT si la TNT explose dans le camp adverse
        e.blockList().forEach(b -> {
            boolean isBlueSide = b.getLocation().getX() < middleX;
            if (team == TeamColor.BLUE && !isBlueSide){
                game.addBrokenBlock(TeamColor.BLUE, b.getLocation());
            } else if (team == TeamColor.RED && isBlueSide){
                game.addBrokenBlock(TeamColor.RED, b.getLocation());
            }
        });
    }
}
