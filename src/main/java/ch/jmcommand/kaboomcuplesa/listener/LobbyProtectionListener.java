package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class LobbyProtectionListener implements Listener {

    private final KaboomCupLesa plugin;

    public LobbyProtectionListener(KaboomCupLesa plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (plugin.game().state() != GameState.RUNNING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEnvDamage(EntityDamageEvent e) {
        if (plugin.game().state() != GameState.RUNNING) {
            // éviter le feu / cactus / etc. au lobby
            e.setCancelled(true);
        }
    }
}
