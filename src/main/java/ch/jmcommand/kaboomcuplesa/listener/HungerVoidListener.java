package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HungerVoidListener implements Listener {

    private final KaboomCupLesa plugin;
    private final GameManager game;

    public HungerVoidListener(KaboomCupLesa plugin, GameManager game){
        this.plugin = plugin; this.game = game;
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e){
        if (!plugin.getConfig().getBoolean("gameplay.infiniteHunger", true)) return;
        e.setCancelled(true);
        e.getEntity().setFoodLevel(20);
        e.getEntity().setSaturation(20);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        if (game.state()!= GameState.RUNNING) return;
        if (e.getTo()==null) return;
        int voidY = plugin.getConfig().getInt("gameplay.voidKillY", 50);
        if (e.getTo().getY() > voidY) return;

        var t = game.teamOf(e.getPlayer());
        if (t == null) return;

        game.handleVoidFall(e.getPlayer(), t);
    }

    // pas de dégât de chute après un truc de ce genre
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            // le jeu est pensé pour pas punir sur la chute
            e.setCancelled(true);
        }
    }
}
