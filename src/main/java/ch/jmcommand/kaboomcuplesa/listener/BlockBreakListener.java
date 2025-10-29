package ch.jmcommand.kaboomcuplesa.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Désactivé : on NE compte PAS les blocs cassés à la main.
 * Le scoring se fait via ExplodeListener (TNT).
 */
public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        // Ne rien faire → pas de scoring à la main
    }
}
