package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class MenuLockListener implements Listener {

    private final KaboomCupLesa plugin;
    private final ch.jmcommand.kaboomcuplesa.game.GameManager game;

    public MenuLockListener(KaboomCupLesa plugin, ch.jmcommand.kaboomcuplesa.game.GameManager game){
        this.plugin = plugin; this.game = game;
    }

    @EventHandler public void onCmd(PlayerCommandPreprocessEvent e){
        if (game.state() != GameState.RUNNING) return;
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/menu")){
            e.setCancelled(true);
            e.getPlayer().sendMessage(plugin.color("&cLe menu est désactivé pendant la partie."));
        }
    }
}
