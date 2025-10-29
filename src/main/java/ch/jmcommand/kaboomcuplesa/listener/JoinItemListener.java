package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.ui.TeamMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class JoinItemListener implements Listener {

    private final KaboomCupLesa plugin;
    private final TeamMenu menu;

    public JoinItemListener(KaboomCupLesa plugin, TeamMenu menu){
        this.plugin = plugin; this.menu = menu;
    }

    public void giveJoinItem(Player p){
        if (plugin.getGameState() != GameState.LOBBY) return;
        var matName = plugin.getConfig().getString("lobby.joinItem.material","COMPASS");
        var name = plugin.color(plugin.getConfig().getString("lobby.joinItem.name","&eKaboomCup &7» &fChoisir une équipe"));
        int slot = plugin.getConfig().getInt("lobby.joinItem.slot", 4);

        Material mat;
        try { mat = Material.valueOf(matName.toUpperCase()); }
        catch (IllegalArgumentException ex){ mat = Material.COMPASS; }

        ItemStack it = new ItemStack(mat, 1);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);

        p.getInventory().setItem(slot, it);
    }

    public void clearJoinItem(Player p){
        var name = plugin.color(plugin.getConfig().getString("lobby.joinItem.name","&eKaboomCup &7» &fChoisir une équipe"));
        p.getInventory().forEach(stack -> {
            if (stack != null && stack.hasItemMeta() && name.equals(stack.getItemMeta().getDisplayName())){
                stack.setAmount(0);
            }
        });
    }

    @EventHandler public void onUse(PlayerInteractEvent e){
        if (plugin.getGameState() != GameState.LOBBY) return;
        if (e.getAction()!=Action.RIGHT_CLICK_AIR && e.getAction()!=Action.RIGHT_CLICK_BLOCK) return;
        var it = e.getItem();
        if (it == null || !it.hasItemMeta() || it.getItemMeta().getDisplayName()==null) return;
        var cfgName = plugin.color(plugin.getConfig().getString("lobby.joinItem.name","&eKaboomCup &7» &fChoisir une équipe"));
        if (!cfgName.equals(it.getItemMeta().getDisplayName())) return;

        e.setCancelled(true);
        menu.open(e.getPlayer());
    }

    @EventHandler public void onDrop(PlayerDropItemEvent e){
        if (plugin.getGameState() != GameState.LOBBY) return;
        var it = e.getItemDrop().getItemStack();
        if (it == null || !it.hasItemMeta()) return;
        var cfgName = plugin.color(plugin.getConfig().getString("lobby.joinItem.name","&eKaboomCup &7» &fChoisir une équipe"));
        if (it.getItemMeta().getDisplayName()!=null && cfgName.equals(it.getItemMeta().getDisplayName())){
            e.setCancelled(true);
        }
    }
}
