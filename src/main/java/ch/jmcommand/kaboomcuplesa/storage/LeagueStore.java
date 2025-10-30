package ch.jmcommand.kaboomcuplesa.storage;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import org.bukkit.configuration.file.FileConfiguration;

public class LeagueStore {

    private final KaboomCupLesa plugin;

    public LeagueStore(KaboomCupLesa plugin) {
        this.plugin = plugin;
        ensure();
    }

    private void ensure() {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.isSet("league.week")) {
            cfg.set("league.week", 1);
        }
        if (!cfg.isSet("league.points.blue")) {
            cfg.set("league.points.blue", 0);
        }
        if (!cfg.isSet("league.points.red")) {
            cfg.set("league.points.red", 0);
        }
        plugin.saveConfig();
    }

    public int getWeek() {
        return plugin.getConfig().getInt("league.week", 1);
    }

    public void setWeek(int week) {
        plugin.getConfig().set("league.week", week);
        plugin.saveConfig();
    }

    public int getPoints(TeamColor color) {
        String path = (color == TeamColor.BLUE) ? "league.points.blue" : "league.points.red";
        return plugin.getConfig().getInt(path, 0);
    }

    public void addPoint(TeamColor color, int pts) {
        String path = (color == TeamColor.BLUE) ? "league.points.blue" : "league.points.red";
        int current = plugin.getConfig().getInt(path, 0);
        plugin.getConfig().set(path, current + pts);
        plugin.saveConfig();
    }

    // 👉 NOUVEAU
    public void reset() {
        plugin.getConfig().set("league.points.blue", 0);
        plugin.getConfig().set("league.points.red", 0);
        plugin.saveConfig();
    }
}
