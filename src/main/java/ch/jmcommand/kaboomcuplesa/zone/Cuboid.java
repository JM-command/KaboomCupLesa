package ch.jmcommand.kaboomcuplesa.zone;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

/**
 * Cuboïde axis-aligned, borné par deux coins inclusifs.
 * Utilisé pour contrôler si un bloc / une location est dans une zone.
 */
public class Cuboid {
    private final World world;
    private final int x1, y1, z1;
    private final int x2, y2, z2;

    public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.world = Objects.requireNonNull(world, "world");
        // normalisation des bornes
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
        this.z2 = Math.max(z1, z2);
    }

    public static Cuboid fromTwoPoints(Location a, Location b) {
        if (a == null || b == null) throw new IllegalArgumentException("Locations null");
        if (a.getWorld() == null || b.getWorld() == null) throw new IllegalArgumentException("World null");
        if (!a.getWorld().equals(b.getWorld())) throw new IllegalArgumentException("Different worlds");
        return new Cuboid(
                a.getWorld(),
                a.getBlockX(), a.getBlockY(), a.getBlockZ(),
                b.getBlockX(), b.getBlockY(), b.getBlockZ()
        );
    }

    public boolean contains(Location l) {
        if (l == null || l.getWorld() == null) return false;
        if (!l.getWorld().equals(world)) return false;
        int x = l.getBlockX(), y = l.getBlockY(), z = l.getBlockZ();
        return x >= x1 && x <= x2 &&
                y >= y1 && y <= y2 &&
                z >= z1 && z <= z2;
    }

    public World world() { return world; }
    public int x1() { return x1; }
    public int y1() { return y1; }
    public int z1() { return z1; }
    public int x2() { return x2; }
    public int y2() { return y2; }
    public int z2() { return z2; }

    public long volume() {
        long dx = (long) x2 - x1 + 1;
        long dy = (long) y2 - y1 + 1;
        long dz = (long) z2 - z1 + 1;
        return dx * dy * dz; // produit en long, safe pour nos tailles de zones
    }


    @Override
    public String toString() {
        return "Cuboid{" + world.getName() + " [" +
                x1 + "," + y1 + "," + z1 + " -> " +
                x2 + "," + y2 + "," + z2 + "]}";
    }
}
