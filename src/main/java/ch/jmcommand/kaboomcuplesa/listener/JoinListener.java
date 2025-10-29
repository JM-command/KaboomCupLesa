package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.team.NametagService;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import ch.jmcommand.kaboomcuplesa.zone.ZoneManager;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final KaboomCupLesa plugin;
    private final ZoneManager zones;
    private final TeamManager teams;
    private final NametagService tags;

    public JoinListener(KaboomCupLesa plugin, ZoneManager zones, TeamManager teams, NametagService tags) {
        this.plugin = plugin;
        this.zones = zones;
        this.teams = teams;
        this.tags = tags;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // pas de message global de join
        e.setJoinMessage(null);

        // Hub d'attente + aventure
        if (zones.spawn() != null) {
            e.getPlayer().teleport(zones.spawn());
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            e.getPlayer().sendMessage(plugin.msg("misc.joinedLobby"));
        }

        // Si le joueur avait déjà une équipe (reload serveur par ex.), réappliquer la couleur TAB + nametag
        var team = teams.get(e.getPlayer());
        if (team != null) {
            tags.apply(e.getPlayer(), team);
        }


    }
}
