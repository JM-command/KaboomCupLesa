package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HungerVoidListener implements Listener {

    private final KaboomCupLesa plugin;
    private final GameManager game;

    public HungerVoidListener(KaboomCupLesa plugin, GameManager game){
        this.plugin = plugin; this.game = game;
    }

    @EventHandler public void onFood(FoodLevelChangeEvent e){
        if (!plugin.getConfig().getBoolean("gameplay.infiniteHunger", true)) return;
        e.setCancelled(true);
        e.getEntity().setFoodLevel(20);
        e.getEntity().setSaturation(20);
    }

    @EventHandler public void onMove(PlayerMoveEvent e){
        if (game.state()!= GameState.RUNNING) return;
        if (e.getTo()==null) return;
        int voidY = plugin.getConfig().getInt("gameplay.voidKillY", 50);
        if (e.getTo().getY() > voidY) return;

        // Punition void: téléport base + décrémenter via DeathService de GameManager
        var t = game.teamOf(e.getPlayer());
        if (t == null) return;

        game.handleVoidFall(e.getPlayer(), t);
    }
}
