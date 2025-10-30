package ch.jmcommand.kaboomcuplesa.kit;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class KitService {

    private final KaboomCupLesa plugin;
    private final Map<UUID, Long> lastGive = new HashMap<>();
    private final long cooldownMs = 3000;

    public KitService(KaboomCupLesa plugin){ this.plugin = plugin; }

    public boolean giveKit(Player p, String key){
        long now = System.currentTimeMillis();
        long last = lastGive.getOrDefault(p.getUniqueId(), 0L);
        if (now - last < cooldownMs){
            double s = (cooldownMs - (now-last)) / 1000.0;
            p.sendMessage(plugin.color("&cAttends encore &e" + String.format("%.1f", s) + "s"));
            return false;
        }
        var sec = plugin.getConfig().getConfigurationSection("kits." + key);
        if (sec == null){
            p.sendMessage(plugin.color("&cKit inconnu: &f" + key));
            return false;
        }
        var list = sec.getStringList("items");
        if (list == null || list.isEmpty()){
            p.sendMessage(plugin.color("&cKit vide: &f" + key));
            return false;
        }
        for (String token : list){
            String[] parts = token.split(":");
            Material mat;
            try { mat = Material.valueOf(parts[0].toUpperCase()); }
            catch (IllegalArgumentException ex){ continue; }
            int qty = 1;
            if (parts.length >= 2) {
                try { qty = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
            }
            while (qty > 0){
                int give = Math.min(qty, mat.getMaxStackSize());
                p.getInventory().addItem(new ItemStack(mat, give));
                qty -= give;
            }
        }
        lastGive.put(p.getUniqueId(), now);
        return true;
    }

    public void giveStartKitIfConfigured(Player p){
        var sec = plugin.getConfig().getConfigurationSection("kits.simple");
        if (sec != null && sec.getBoolean("giveOnStart", true)){
            giveKit(p, "simple");
        }
    }

    public void giveHubItem(Player p){
        var matName = plugin.getConfig().getString("lobby.joinItem.material", "COMPASS");
        var name = plugin.color(plugin.getConfig().getString("lobby.joinItem.name", "&eKaboomCup &7» &fChoisir une équipe"));
        int slot = plugin.getConfig().getInt("lobby.joinItem.slot", 4);

        Material mat;
        try { mat = Material.valueOf(matName.toUpperCase()); }
        catch (IllegalArgumentException ex){ mat = Material.COMPASS; }

        ItemStack it = new ItemStack(mat, 1);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);
        p.getInventory().setItem(slot, it);
    }

    public void clearHubItem(Player p){
        var name = plugin.color(plugin.getConfig().getString("lobby.joinItem.name", "&eKaboomCup &7» &fChoisir une équipe"));

        // inventaire normal
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (stack != null && stack.hasItemMeta() && name.equals(stack.getItemMeta().getDisplayName())){
                p.getInventory().setItem(i, null);
            }
        }
        // offhand
        ItemStack off = p.getInventory().getItemInOffHand();
        if (off != null && off.hasItemMeta() && name.equals(off.getItemMeta().getDisplayName())) {
            p.getInventory().setItemInOffHand(null);
        }
    }
}
