package ch.jmcommand.kaboomcuplesa.ui;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.team.NametagService;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamMenu {

    private final KaboomCupLesa plugin;
    private final TeamManager teams;
    private final NametagService tags;

    public TeamMenu(KaboomCupLesa plugin, TeamManager teams, NametagService tags) {
        this.plugin = plugin;
        this.teams = teams;
        this.tags = tags;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(
                p,
                9,
                plugin.color(plugin.messages().getString("titles.teamMenu", "Choisis ton équipe"))
        );
        inv.setItem(3, item(Material.BLUE_WOOL, plugin.color(plugin.messages().getString("gui.joinBlue"))));
        inv.setItem(5, item(Material.RED_WOOL, plugin.color(plugin.messages().getString("gui.joinRed"))));
        p.openInventory(inv);
    }

    private ItemStack item(Material m, String name) {
        ItemStack it = new ItemStack(m);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);
        return it;
    }

    public void joinBlue(Player p) {
        if (teams.isTeamFull(TeamColor.BLUE)) {
            p.sendMessage(plugin.color(plugin.messages().getString("errors.teamFull")
                    .replace("{max}", String.valueOf(teams.maxPerTeam()))));
            return;
        }
        tags.apply(p, TeamColor.BLUE);
        teams.join(p, TeamColor.BLUE);
        p.sendMessage(plugin.color(plugin.messages().getString("gui.joinedBlue")));
    }

    public void joinRed(Player p) {
        if (teams.isTeamFull(TeamColor.RED)) {
            p.sendMessage(plugin.color(plugin.messages().getString("errors.teamFull")
                    .replace("{max}", String.valueOf(teams.maxPerTeam()))));
            return;
        }
        tags.apply(p, TeamColor.RED);
        teams.join(p, TeamColor.RED);
        p.sendMessage(plugin.color(plugin.messages().getString("gui.joinedRed")));
    }
}
