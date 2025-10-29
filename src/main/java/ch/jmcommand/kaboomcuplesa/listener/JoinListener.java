package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.zone.ZoneManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final KaboomCupLesa plugin;
    private final ZoneManager zones;

    public JoinListener(KaboomCupLesa p, ZoneManager z){
        this.plugin = p; this.zones = z;
    }

    @EventHandler public void onJoin(PlayerJoinEvent e){
        if (zones.spawn()!=null && KaboomCupLesa.get().getServer()!=null && KaboomCupLesa.get().getConfig().getBoolean("teleportToSpawnOnJoin", true)){
            e.getPlayer().teleport(zones.spawn());
            e.getPlayer().sendMessage(KaboomCupLesa.get().msg("misc.joinedLobby"));
        }
    }

}
