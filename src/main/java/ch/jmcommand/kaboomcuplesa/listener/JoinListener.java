package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.kit.KitService;
import ch.jmcommand.kaboomcuplesa.team.NametagService;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final KaboomCupLesa plugin;
    private final TeamManager teams;
    private final NametagService tags;
    private final KitService kits;

    public JoinListener(KaboomCupLesa plugin, TeamManager teams, NametagService tags, KitService kits) {
        this.plugin = plugin;
        this.teams = teams;
        this.tags = tags;
        this.kits = kits;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);

        // TP spawn si configuré
        var sp = plugin.game().spawn();
        if (sp != null) e.getPlayer().teleport(sp);
        e.getPlayer().setGameMode(GameMode.ADVENTURE);

        // Donner l'item hub si LOBBY
        if (plugin.game().state() == GameState.LOBBY) {
            kits.giveHubItem(e.getPlayer());
        }

        // Recolorer si déjà en team (reload, etc.)
        var t = teams.get(e.getPlayer());
        if (t != null) tags.apply(e.getPlayer(), t);
    }
}
