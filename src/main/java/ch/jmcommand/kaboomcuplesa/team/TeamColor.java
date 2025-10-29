package ch.jmcommand.kaboomcuplesa.team;

import org.bukkit.ChatColor;

public enum TeamColor {
    BLUE(ChatColor.BLUE, "BLUE"),
    RED(ChatColor.RED, "RED");

    public final ChatColor color;
    public final String key;

    TeamColor(ChatColor c, String key){
        this.color = c;
        this.key = key;
    }

    public String display(){
        return color + "" + ChatColor.BOLD + key + ChatColor.RESET;
    }
}
