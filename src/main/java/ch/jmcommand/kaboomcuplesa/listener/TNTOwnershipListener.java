package ch.jmcommand.kaboomcuplesa.listener;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TNTOwnershipListener implements Listener {

    private final KaboomCupLesa plugin;
    private final TeamManager teams;
    private final NamespacedKey ownerKey;

    // On stocke "qui a posé quel bloc de TNT" (coordonnées de bloc → UUID joueur)
    private final Map<String, UUID> tntPlacedBy = new ConcurrentHashMap<>();

    public TNTOwnershipListener(KaboomCupLesa plugin, TeamManager teams) {
        this.plugin = plugin;
        this.teams = teams;
        this.ownerKey = new NamespacedKey(plugin, "tnt_owner");
    }

    // Clé stable "world@x:y:z" pour les locs de bloc
    private String keyOf(Block b) {
        Location l = b.getLocation();
        return l.getWorld().getName() + "@" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    private String keyOf(Location l) {
        return l.getWorld().getName() + "@" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced() == null) return;
        if (e.getBlockPlaced().getType() != org.bukkit.Material.TNT) return;
        // Mémorise le poseur de CE bloc de TNT
        tntPlacedBy.put(keyOf(e.getBlockPlaced()), e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTntSpawn(EntitySpawnEvent e) {
        Entity ent = e.getEntity();
        if (!(ent instanceof TNTPrimed)) return;

        // Recherche un owner fiable:
        // 1) si le block de TNT vient d’être primé, sa loc "bloc" = loc de spawn entité
        UUID who = tntPlacedBy.remove(keyOf(ent.getLocation()));

        // 2) fallback Paper (si présent) : source vivante (ex: joueur qui a allumé au briquet)
        // (compatible Spigot: getSource peut ne pas exister; safe cast check)
        if (who == null) {
            try {
                Entity src = ((TNTPrimed) ent).getSource();
                if (src instanceof Player) who = ((Player) src).getUniqueId();
            } catch (NoSuchMethodError ignored) {
                // Spigot pur: pas de getSource()
            }
        }

        if (who != null) {
            ent.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, who.toString());
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed)) return;

        // Récupère owner si existant
        String ownerStr = e.getEntity().getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
        if (ownerStr == null) return;

        UUID ownerId;
        try { ownerId = UUID.fromString(ownerStr); } catch (IllegalArgumentException ex) { return; }

        Player owner = plugin.getServer().getPlayer(ownerId);
        TeamColor ownerTeam = (owner == null ? null : teams.get(owner));
        if (ownerTeam == null) return;

        // Option friendly-fire par côté (basée sur field.middleX)
        boolean preventFF = plugin.getConfig().getBoolean("tnt.preventFriendlyFireBySide", true);
        if (!preventFF) return;

        double middleX = plugin.getConfig().getDouble("field.middleX", 0.0);
        double expX = e.getLocation().getX();

        // Détermine le "côté" du point d’explosion
        // Convention: X < middleX = Blue-side ; X >= middleX = Red-side
        TeamColor side =
                (expX < middleX) ? TeamColor.BLUE : TeamColor.RED;

        // Si l’explosion se produit sur le même côté que l’owner → on annule la casse de blocs (friendly fire off)
        if (side == ownerTeam) {
            e.blockList().clear(); // on laisse l’effet visuel/knockback, mais pas de destruction
        }
    }
}
