package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerListener implements Listener {
    private final KaboomCupLesa plugin;
    public HungerListener(KaboomCupLesa plugin){ this.plugin = plugin; }

    @EventHandler public void onFood(FoodLevelChangeEvent e){
        if (!plugin.getConfig().getBoolean("gameplay.infiniteHunger", true)) return;
        e.setCancelled(true);
        e.getEntity().setFoodLevel(20);
        e.getEntity().setSaturation(20);
    }
}
