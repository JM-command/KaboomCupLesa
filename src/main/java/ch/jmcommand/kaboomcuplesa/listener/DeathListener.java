package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.game.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    private final GameManager game;
    public DeathListener(GameManager g){ this.game = g; }

    @EventHandler public void onDeath(PlayerDeathEvent e){
        game.onPlayerDeath(e.getEntity());
    }
}
