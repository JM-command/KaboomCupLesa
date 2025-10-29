package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.ui.TeamMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class HubItemListener implements Listener {

    private final KaboomCupLesa plugin;
    private final ch.jmcommand.kaboomcuplesa.game.GameManager game;
    private final TeamMenu menu;

    public HubItemListener(KaboomCupLesa plugin, ch.jmcommand.kaboomcuplesa.game.GameManager game, TeamMenu menu){
        this.plugin = plugin; this.game = game; this.menu = menu;
    }

    @EventHandler public void onUse(PlayerInteractEvent e){
        if (game.state() != GameState.LOBBY) return;
        if (e.getAction()!= Action.RIGHT_CLICK_AIR && e.getAction()!= Action.RIGHT_CLICK_BLOCK) return;
        var it = e.getItem();
        if (it == null || !it.hasItemMeta() || it.getItemMeta().getDisplayName()==null) return;
        var cfgName = plugin.color(plugin.getConfig().getString("lobby.joinItem.name","&eKaboomCup &7» &fChoisir une équipe"));
        if (!cfgName.equals(it.getItemMeta().getDisplayName())) return;
        e.setCancelled(true);
        menu.open(e.getPlayer());
    }

    @EventHandler public void onDrop(PlayerDropItemEvent e){
        if (game.state()!= GameState.LOBBY) return;
        var it = e.getItemDrop().getItemStack();
        if (it == null || !it.hasItemMeta() || it.getItemMeta().getDisplayName()==null) return;
        var cfgName = plugin.color(plugin.getConfig().getString("lobby.joinItem.name","&eKaboomCup &7» &fChoisir une équipe"));
        if (cfgName.equals(it.getItemMeta().getDisplayName())){
            e.setCancelled(true);
        }
    }
}
