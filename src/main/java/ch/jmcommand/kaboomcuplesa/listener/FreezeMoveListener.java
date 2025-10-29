package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class FreezeMoveListener implements Listener {
    private final GameManager game;
    public FreezeMoveListener(GameManager game){ this.game = game; }

    @EventHandler public void onMove(PlayerMoveEvent e){
        if (game.state()!= GameState.LOBBY && !game.isFreezeActive()) return;
        // freeze actif : autoriser rotations mais pas changement de X/Z
        if (!game.isFreezeActive()) return;
        if (e.getFrom().getX()!= e.getTo().getX() || e.getFrom().getZ()!= e.getTo().getZ()){
            e.setTo(e.getFrom().clone().setDirection(e.getTo().getDirection()));
        }
    }
}
