package ch.jmcommand.kaboomcuplesa.team;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.*;

public class NametagService {

    private final KaboomCupLesa plugin;
    private final Scoreboard sb;
    private final Team blueTeam;
    private final Team redTeam;

    public NametagService(KaboomCupLesa p){
        this.plugin = p;
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        this.sb = sm.getMainScoreboard();

        Team bt = sb.getTeam("KAB_BLUE");
        if (bt==null) bt = sb.registerNewTeam("KAB_BLUE");
        bt.setColor(ChatColor.BLUE);
        bt.setCanSeeFriendlyInvisibles(false);
        bt.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        this.blueTeam = bt;

        Team rt = sb.getTeam("KAB_RED");
        if (rt==null) rt = sb.registerNewTeam("KAB_RED");
        rt.setColor(ChatColor.RED);
        rt.setCanSeeFriendlyInvisibles(false);
        rt.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        this.redTeam = rt;
    }

    public void apply(Player p, TeamColor color){
        blueTeam.removeEntry(p.getName());
        redTeam.removeEntry(p.getName());

        if (color==TeamColor.BLUE){
            blueTeam.addEntry(p.getName());
        } else if (color==TeamColor.RED){
            redTeam.addEntry(p.getName());
        }
        p.setPlayerListName((color==TeamColor.BLUE? ChatColor.BLUE: ChatColor.RED) + p.getName());

        // équipe -> armure (si activée)
        if (plugin.getConfig().getBoolean("gameplay.teamArmor", true)){
            equipArmor(p, color);
        }
    }

    public void clear(Player p){
        blueTeam.removeEntry(p.getName());
        redTeam.removeEntry(p.getName());
        p.setPlayerListName(p.getName());
        // enlève l’armure d’équipe si activée
        if (plugin.getConfig().getBoolean("gameplay.teamArmor", true)){
            unequipArmor(p);
        }
    }

    private ItemStack dyed(Material mat, Color c){
        ItemStack it = new ItemStack(mat, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) it.getItemMeta();
        meta.setColor(c);
        it.setItemMeta(meta);
        return it;
    }

    public void equipArmor(Player p, TeamColor color){
        Color c = (color==TeamColor.BLUE) ? Color.BLUE : Color.RED;
        ItemStack chest = dyed(Material.LEATHER_CHESTPLATE, c);
        ItemStack legs  = dyed(Material.LEATHER_LEGGINGS, c);
        ItemStack boots = dyed(Material.LEATHER_BOOTS, c);
        // On n’écrase pas un stuff déjà présent important ; ici on remplace pour la lisibilité
        p.getInventory().setChestplate(chest);
        p.getInventory().setLeggings(legs);
        p.getInventory().setBoots(boots);
    }

    public void unequipArmor(Player p){
        // On enlève seulement si c’est du cuir (notre set)
        var inv = p.getInventory();
        if (inv.getChestplate()!=null && inv.getChestplate().getType()==Material.LEATHER_CHESTPLATE) inv.setChestplate(null);
        if (inv.getLeggings()!=null && inv.getLeggings().getType()==Material.LEATHER_LEGGINGS) inv.setLeggings(null);
        if (inv.getBoots()!=null && inv.getBoots().getType()==Material.LEATHER_BOOTS) inv.setBoots(null);
    }
}
