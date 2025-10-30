package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stocke le DERNIER joueur qui a frappé un autre joueur.
 * Servira surtout pour les morts "dans le vide" pour afficher
 * "JM a poussé André dans le vide".
 */
public class DamageTrackerListener implements Listener {

    private final KaboomCupLesa plugin;

    // victim -> last hit (attacker, time)
    private final Map<UUID, LastHit> lastHits = new ConcurrentHashMap<>();

    // durée max entre le coup et la chute (ms)
    private final long maxDelayMs = 6000L; // 6s

    public DamageTrackerListener(KaboomCupLesa plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();
        if (!(victim instanceof Player)) return;

        Player attacker = null;

        // Cas simple: un joueur tape un joueur
        if (e.getDamager() instanceof Player) {
            attacker = (Player) e.getDamager();
        } else {
            // plus tard: flèches, projectiles, TNT primed avec owner, etc.
            // pour l’instant on garde simple
        }

        if (attacker == null) return;

        lastHits.put(victim.getUniqueId(),
                new LastHit(attacker.getUniqueId(), System.currentTimeMillis()));
    }

    /**
     * Récupère le dernier joueur qui a frappé "victimId"
     * si c'était il y a moins de maxDelayMs.
     */
    public UUID getLastDamager(UUID victimId) {
        LastHit lh = lastHits.get(victimId);
        if (lh == null) return null;
        if (System.currentTimeMillis() - lh.timeMs > maxDelayMs) return null;
        return lh.attacker;
    }

    private static class LastHit {
        final UUID attacker;
        final long timeMs;
        LastHit(UUID attacker, long timeMs) {
            this.attacker = attacker;
            this.timeMs = timeMs;
        }
    }
}
