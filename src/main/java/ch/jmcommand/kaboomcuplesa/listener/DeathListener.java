package ch.jmcommand.kaboomcuplesa.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        // on masque les msgs moches
        e.setDeathMessage(null);
        // tu pourras plus tard brancher ici le "X a poussé Y dans le vide"
    }
}
