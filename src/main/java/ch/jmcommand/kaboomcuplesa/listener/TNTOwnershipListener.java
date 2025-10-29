package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import io.papermc.paper.event.block.TNTPrimeEvent; // Paper API
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class TNTOwnershipListener implements Listener {

    private final KaboomCupLesa plugin;
    private final TeamManager teams;
    private final GameManager game;

    // map temporaire: position block TNT placé -> équipe
    private final Map<String, TeamColor> placedTNT = new HashMap<>();

    private final NamespacedKey OWNER_KEY;

    public TNTOwnershipListener(KaboomCupLesa plugin, TeamManager teams, GameManager game){
        this.plugin = plugin;
        this.teams = teams;
        this.game = game;
        this.OWNER_KEY = new NamespacedKey(plugin, "kaboom_owner_team");
    }

    private String key(Block b){
        var l = b.getLocation();
        return l.getWorld().getName()+":"+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
    }

    /** Au placement d'un bloc TNT, mémorise l'équipe du joueur */
    @EventHandler public void onPlace(BlockPlaceEvent e){
        if (e.getBlockPlaced()==null) return;
        if (e.getBlockPlaced().getType()!= org.bukkit.Material.TNT) return;
        TeamColor t = teams.get(e.getPlayer());
        if (t==null) return;
        placedTNT.put(key(e.getBlockPlaced()), t);
    }

    /** Quand la TNT est "primée", on transfère l'owner vers l'entité TNTPrimed */
    @EventHandler public void onPrime(TNTPrimeEvent e){
        Block b = e.getBlock();
        TeamColor t = placedTNT.remove(key(b));
        if (t == null) return;

        Entity ent = e.getPrimedTnt();
        if (ent instanceof TNTPrimed tnt){
            // PDC tag
            PersistentDataContainer pdc = tnt.getPersistentDataContainer();
            pdc.set(OWNER_KEY, PersistentDataType.STRING, t.name());
            // Nom custom (invisible) pour debug
            tnt.customName(plugin.color((t == TeamColor.BLUE ? "&9BLUE" : "&cRED")));
            tnt.setCustomNameVisible(false);
        }
    }

    /** Friendly-fire TNT côté propre: on supprime les blocks affectés si sur le même côté */
    @EventHandler public void onExplode(EntityExplodeEvent e){
        if (e.getEntityType()!= EntityType.PRIMED_TNT) return;

        boolean ffOff = !plugin.getConfig().getBoolean("gameplay.friendlyFireTNT", false) ? true : false;
        if (!ffOff) return;

        TeamColor owner = getOwner((TNTPrimed) e.getEntity());
        if (owner == null) return;

        // Détermine le côté via field.middleX
        double middleX = plugin.getConfig().getDouble("field.middleX", 0.0);
        boolean explosionOnBlueSide = e.getLocation().getX() < middleX;

        // Si TNT du bleu sur côté bleu → retire la casse de blocks
        if (owner == TeamColor.BLUE && explosionOnBlueSide) {
            e.blockList().clear();
        }
        // Si TNT du rouge sur côté rouge → retire la casse de blocks
        if (owner == TeamColor.RED && !explosionOnBlueSide) {
            e.blockList().clear();
        }
    }

    /** Empêche dégâts alliés par TNT si friendlyFireTNT=false */
    @EventHandler public void onDamage(EntityDamageByEntityEvent e){
        if (!(e.getDamager() instanceof TNTPrimed tnt)) return;
        boolean ffOff = !plugin.getConfig().getBoolean("gameplay.friendlyFireTNT", false) ? true : false;
        if (!ffOff) return;

        TeamColor owner = getOwner(tnt);
        if (owner == null) return;

        if (e.getEntity() instanceof org.bukkit.entity.Player p){
            TeamColor victim = teams.get(p);
            if (victim != null && victim == owner){
                e.setCancelled(true);
            }
        }
    }

    private TeamColor getOwner(TNTPrimed tnt){
        try {
            String val = tnt.getPersistentDataContainer().get(OWNER_KEY, PersistentDataType.STRING);
            if (val == null) return null;
            return TeamColor.valueOf(val);
        } catch (Throwable ignored){
            return null;
        }
    }
}
