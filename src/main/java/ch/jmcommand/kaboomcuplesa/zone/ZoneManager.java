package ch.jmcommand.kaboomcuplesa.zone;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Charge et expose les positions/zones depuis config.yml
 * - Monde, spawn, baseBlue, baseRed
 * - Cuboïdes: blueZone, redZone, wallZone, (blueSupply, redSupply)
 * Fournit aussi des setters qui écrivent en config et sauvegardent.
 */
public class ZoneManager {

    public enum ZoneType {
        BLUE_ZONE("blueZone"),
        RED_ZONE("redZone"),
        WALL_ZONE("wallZone"),
        BLUE_SUPPLY("blueSupply"),
        RED_SUPPLY("redSupply");

        public final String pathKey;
        ZoneType(String key){ this.pathKey = key; }
    }

    private final KaboomCupLesa plugin;

    private World world;
    private Location spawn;
    private Location baseBlue;
    private Location baseRed;

    private final Map<ZoneType, Cuboid> zones = new EnumMap<>(ZoneType.class);

    public ZoneManager(KaboomCupLesa plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        reload();
    }

    /* =======================
     * Chargement / Reload
     * ======================= */

    public final void reload() {
        // Monde
        String worldName = plugin.getConfig().getString("world", "tntwars");
        this.world = Bukkit.getWorld(worldName);
        if (this.world == null) {
            plugin.warn("Monde '" + worldName + "' introuvable. Certains TP échoueront tant que le monde n'est pas chargé.");
        }

        // Localisations
        this.spawn    = readLocation("spawn");
        this.baseBlue = readLocation("baseBlue");
        this.baseRed  = readLocation("baseRed");

        // Zones (cuboïdes)
        zones.clear();
        loadCuboid(ZoneType.BLUE_ZONE);
        loadCuboid(ZoneType.RED_ZONE);
        loadCuboid(ZoneType.WALL_ZONE);
        loadCuboid(ZoneType.BLUE_SUPPLY);
        loadCuboid(ZoneType.RED_SUPPLY);
    }

    private void loadCuboid(ZoneType type) {
        Cuboid c = readCuboid("zones." + type.pathKey);
        if (c != null) zones.put(type, c);
    }

    /* =======================
     * Getters principaux
     * ======================= */

    public World world() { return world; }
    public Location spawn() { return spawn; }
    public Location baseBlue() { return baseBlue; }
    public Location baseRed() { return baseRed; }

    public Cuboid blueZone() { return zones.get(ZoneType.BLUE_ZONE); }
    public Cuboid redZone() { return zones.get(ZoneType.RED_ZONE); }
    public Cuboid wallZone() { return zones.get(ZoneType.WALL_ZONE); }
    public Cuboid blueSupply() { return zones.get(ZoneType.BLUE_SUPPLY); }
    public Cuboid redSupply() { return zones.get(ZoneType.RED_SUPPLY); }

    public boolean hasAllCoreZones() {
        return blueZone() != null && redZone() != null && wallZone() != null;
    }

    /* =======================
     * Ecriture config (setup)
     * ======================= */

    public void setWorldName(String name){
        plugin.getConfig().set("world", name);
        plugin.saveConfig();
        reload();
    }

    public void setSpawn(Location loc) {
        writeLocation("spawn", loc);
        this.spawn = loc;
    }

    public void setBaseBlue(Location loc) {
        writeLocation("baseBlue", loc);
        this.baseBlue = loc;
    }

    public void setBaseRed(Location loc) {
        writeLocation("baseRed", loc);
        this.baseRed = loc;
    }

    /**
     * Ecrit une zone cuboïde dans la config: zones.<key>.(x1..z2)
     */
    public void setZone(ZoneType type, Location a, Location b) {
        if (a == null || b == null) throw new IllegalArgumentException("Locations null");
        if (a.getWorld() == null || b.getWorld() == null) throw new IllegalArgumentException("World null");
        if (!a.getWorld().equals(b.getWorld())) throw new IllegalArgumentException("Different worlds");

        String base = "zones." + type.pathKey;
        int x1 = a.getBlockX(), y1 = a.getBlockY(), z1 = a.getBlockZ();
        int x2 = b.getBlockX(), y2 = b.getBlockY(), z2 = b.getBlockZ();

        // normalisation
        int nx1 = Math.min(x1, x2), ny1 = Math.min(y1, y2), nz1 = Math.min(z1, z2);
        int nx2 = Math.max(x1, x2), ny2 = Math.max(y1, y2), nz2 = Math.max(z1, z2);

        plugin.getConfig().set(base + ".x1", nx1);
        plugin.getConfig().set(base + ".y1", ny1);
        plugin.getConfig().set(base + ".z1", nz1);
        plugin.getConfig().set(base + ".x2", nx2);
        plugin.getConfig().set(base + ".y2", ny2);
        plugin.getConfig().set(base + ".z2", nz2);
        plugin.saveConfig();

        // mise à jour mémoire
        if (a.getWorld() != null) this.world = a.getWorld(); // au cas où
        zones.put(type, new Cuboid(a.getWorld(), nx1, ny1, nz1, nx2, ny2, nz2));
    }

    /* =======================
     * Utils de lecture/écriture
     * ======================= */

    private Location readLocation(String path) {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection(path);
        if (s == null) return null;

        World w = world;
        if (w == null) {
            // tente quand même de récupérer un world si disponible plus tard
            String worldName = plugin.getConfig().getString("world", "tntwars");
            w = Bukkit.getWorld(worldName);
        }
        if (w == null) return null;

        double x = s.getDouble("x", 0);
        double y = s.getDouble("y", 80);
        double z = s.getDouble("z", 0);
        float yaw = (float) s.getDouble("yaw", 0.0);
        float pitch = (float) s.getDouble("pitch", 0.0);
        return new Location(w, x, y, z, yaw, pitch);
    }

    private void writeLocation(String path, Location l) {
        if (l == null || l.getWorld() == null) return;
        plugin.getConfig().set(path + ".x", l.getX());
        plugin.getConfig().set(path + ".y", l.getY());
        plugin.getConfig().set(path + ".z", l.getZ());
        plugin.getConfig().set(path + ".yaw", l.getYaw());
        plugin.getConfig().set(path + ".pitch", l.getPitch());
        // met à jour le monde si besoin
        plugin.getConfig().set("world", l.getWorld().getName());
        plugin.saveConfig();
        this.world = l.getWorld();
    }

    private Cuboid readCuboid(String path) {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection(path);
        if (s == null) return null;
        if (world == null) return null;

        int x1 = s.getInt("x1", Integer.MIN_VALUE);
        int y1 = s.getInt("y1", Integer.MIN_VALUE);
        int z1 = s.getInt("z1", Integer.MIN_VALUE);
        int x2 = s.getInt("x2", Integer.MIN_VALUE);
        int y2 = s.getInt("y2", Integer.MIN_VALUE);
        int z2 = s.getInt("z2", Integer.MIN_VALUE);

        if (x1 == Integer.MIN_VALUE || y1 == Integer.MIN_VALUE || z1 == Integer.MIN_VALUE ||
                x2 == Integer.MIN_VALUE || y2 == Integer.MIN_VALUE || z2 == Integer.MIN_VALUE) {
            return null;
        }
        return new Cuboid(world, x1, y1, z1, x2, y2, z2);
    }

    /* =======================
     * Helpers validation
     * ======================= */

    public boolean isConfiguredForStart() {
        if (world == null || spawn == null || baseBlue == null || baseRed == null) return false;
        return hasAllCoreZones();
    }
}
