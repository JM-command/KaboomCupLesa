package ch.jmcommand.kaboomcuplesa.game;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Pattern;

public class KitManager {
    private final KaboomCupLesa plugin;
    private final Map<UUID, Long> lastGive = new HashMap<>();
    private final long cooldownMs = 3000; // 3s anti-spam

    public KitManager(KaboomCupLesa plugin){ this.plugin = plugin; }

    public boolean giveKit(Player p, String key){
        // cooldown
        long now = System.currentTimeMillis();
        long last = lastGive.getOrDefault(p.getUniqueId(), 0L);
        if (now - last < cooldownMs){
            p.sendMessage(plugin.color("&cTrop rapide. Réessaie dans " + ((cooldownMs - (now-last))/1000.0) + "s"));
            return false;
        }
        var sec = plugin.getConfig().getConfigurationSection("kits." + key);
        if (sec == null){
            p.sendMessage(plugin.color("&cKit introuvable: " + key));
            return false;
        }
        var list = sec.getStringList("items");
        if (list == null || list.isEmpty()){
            p.sendMessage(plugin.color("&cKit vide: " + key));
            return false;
        }

        for (String token : list){
            // format "MATERIAL:QTY"
            var parts = token.split(":");
            Material m;
            try { m = Material.valueOf(parts[0].toUpperCase(Locale.ROOT)); }
            catch (IllegalArgumentException ex){ continue; }
            int qty = 1;
            if (parts.length>=2){
                try { qty = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored){}
            }
            while (qty > 0){
                int give = Math.min(qty, m.getMaxStackSize());
                p.getInventory().addItem(new ItemStack(m, give));
                qty -= give;
            }
        }
        lastGive.put(p.getUniqueId(), now);
        return true;
    }

    public boolean giveStartKitIfConfigured(Player p){
        var sec = plugin.getConfig().getConfigurationSection("kits.simple");
        if (sec != null && sec.getBoolean("giveOnStart", true)){
            return giveKit(p, "simple");
        }
        return false;
    }
}
