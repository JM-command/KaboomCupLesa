package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidListener implements Listener {

    private final GameManager game;
    private final TeamManager teams;
    private final int voidY;

    public VoidListener(GameManager game, TeamManager teams, int voidY){
        this.game = game; this.teams = teams; this.voidY = voidY;
    }

    @EventHandler public void onMove(PlayerMoveEvent e){
        if (game.state()!= GameState.RUNNING) return;
        if (e.getTo()==null) return;
        if (e.getTo().getY() > voidY) return;

        var t = teams.get(e.getPlayer());
        if (t == null) return;
        // -1 vie + TP base (sans écran de mort)
        game.voidFallPunish(e.getPlayer(), t);
    }
}
