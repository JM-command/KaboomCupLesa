package ch.jmcommand.kaboomcuplesa.storage;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class LeagueStore {

    private final KaboomCupLesa plugin;

    public LeagueStore(KaboomCupLesa p){
        this.plugin = Objects.requireNonNull(p);
        ensureDefaults();
    }

    private void ensureDefaults(){
        if (plugin.getConfig().getConfigurationSection("league") == null){
            plugin.getConfig().createSection("league");
        }
        ConfigurationSection league = plugin.getConfig().getConfigurationSection("league");
        if (!league.isSet("week")) league.set("week", 1);
        if (league.getConfigurationSection("points") == null){
            league.createSection("points");
        }
        ConfigurationSection pts = league.getConfigurationSection("points");
        if (!pts.isSet("blue")) pts.set("blue", 0);
        if (!pts.isSet("red"))  pts.set("red", 0);
        plugin.saveConfig();
    }

    public int getWeek(){
        return plugin.getConfig().getInt("league.week", 1);
    }

    public void setWeek(int week){
        plugin.getConfig().set("league.week", week);
        plugin.saveConfig();
    }

    public int getPoints(TeamColor team){
        String key = team==TeamColor.BLUE ? "blue" : "red";
        return plugin.getConfig().getInt("league.points." + key, 0);
    }

    public void setPoints(TeamColor team, int value){
        String key = team==TeamColor.BLUE ? "blue" : "red";
        plugin.getConfig().set("league.points." + key, value);
        plugin.saveConfig();
    }

    public void addPoint(TeamColor team, int delta){
        setPoints(team, getPoints(team) + delta);
    }

    public void reload(){
        plugin.reloadConfig();
        ensureDefaults();
    }
}
