package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.ui.TeamMenu;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {
    private final TeamMenu menu;
    public InventoryListener(TeamMenu m){ this.menu = m; }

    @EventHandler public void onClick(InventoryClickEvent e){
        if (e.getView()==null || e.getView().getTitle()==null) return;
        if (!e.getView().getTitle().equals(menuTitle())) return;
        e.setCancelled(true);
        if (e.getCurrentItem()==null) return;

        if (e.getCurrentItem().getType() == Material.BLUE_WOOL){
            menu.joinBlue((org.bukkit.entity.Player)e.getWhoClicked());
            e.getWhoClicked().closeInventory();
        } else if (e.getCurrentItem().getType() == Material.RED_WOOL){
            menu.joinRed((org.bukkit.entity.Player)e.getWhoClicked());
            e.getWhoClicked().closeInventory();
        }
    }

    private String menuTitle(){
        // recréer le titre comme dans TeamMenu.open
        return ch.jmcommand.kaboomcuplesa.KaboomCupLesa.get().color(
                ch.jmcommand.kaboomcuplesa.KaboomCupLesa.get().messages().getString("titles.teamMenu","Choisis ton équipe")
        );
    }
}
