package ch.jmcommand.kaboomcuplesa.scoreboard;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import ch.jmcommand.kaboomcuplesa.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class Sidebar {

    private final KaboomCupLesa plugin;
    private final GameManager game;

    private final Scoreboard sb;
    private final Objective obj;
    private final Map<String, Team> lineTeams = new HashMap<>();

    public Sidebar(KaboomCupLesa plugin, GameManager game){
        this.plugin = plugin;
        this.game = game;

        ScoreboardManager sm = Bukkit.getScoreboardManager();
        this.sb = sm.getNewScoreboard();
        this.obj = sb.registerNewObjective(
                "kaboom","dummy",
                plugin.color("&lKaboomCup Lesa")
        );
        this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        createLine(8, "L0");
        createLine(7, "L1");
        createLine(6, "L2");
        createLine(5, "L3");
        createLine(4, "L4");
        createLine(3, "L5");
        createLine(2, "L6");
        createLine(1, "L7");

        for (Player pl : Bukkit.getOnlinePlayers()) pl.setScoreboard(sb);

        Bukkit.getScheduler().runTaskTimer(plugin, this::update, 0L, 20L);
    }

    private void createLine(int score, String key){
        String entry = "§" + Integer.toHexString(score);
        Team team = sb.registerNewTeam("LINE_" + score);
        team.addEntry(entry);
        obj.getScore(entry).setScore(score);
        lineTeams.put(key, team);
    }

    private void set(String key, String text){
        Team t = lineTeams.get(key);
        if (t == null) return;
        if (text == null) text = "";
        if (text.length() > 32){
            String pre = text.substring(0, Math.min(16, text.length()));
            String suf = text.substring(Math.min(16, text.length()));
            t.setPrefix(plugin.color(pre));
            t.setSuffix(plugin.color(suf));
        } else {
            t.setPrefix("");
            t.setSuffix(plugin.color(text));
        }
    }

    public void update(){
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) return;

        // league infos
        int week = plugin.league().getWeek();
        int bluePts = plugin.league().getPoints(TeamColor.BLUE);
        int redPts  = plugin.league().getPoints(TeamColor.RED);
        String leader;
        if (bluePts == redPts) {
            leader = plugin.msg("league.leaderTie");
        } else if (bluePts > redPts) {
            leader = plugin.msg("league.leaderBlue", Map.of("points", String.valueOf(bluePts)));
        } else {
            leader = plugin.msg("league.leaderRed", Map.of("points", String.valueOf(redPts)));
        }

        if (game.state() == GameState.LOBBY || game.state() == GameState.PAUSED){
            set("L0", plugin.msg("league.sidebarWeek", Map.of("week", String.valueOf(week))));
            set("L1", leader);
            set("L2", "&7Etat: &f" + game.state().name());
            set("L3", "&9Blue joueurs: &f" + game.teamSizeBlue());
            set("L4", "&cRed  joueurs: &f" + game.teamSizeRed());
            set("L5", "&7Tip: &fClique la boussole &7(ou /menu)");
            set("L6", plugin.color("&7" + plugin.getConfig().getString("scoreboard.sponsorFooter","")));
            set("L7", "");
        } else {
            int secs = game.secondsLeft();
            String mm = String.format("%02d", secs/60);
            String ss = String.format("%02d", secs%60);
            set("L0", plugin.msg("league.sidebarWeek", Map.of("week", String.valueOf(week))));
            set("L1", leader);
            set("L2", "&7Etat: &aRUNNING");
            set("L3", plugin.getConfig().getBoolean("scoreboard.showTimer", true) ? "&7Timer: &f" + mm + ":" + ss : "");
            set("L4", "&9Blue vies: &f" + game.livesStringBlue());
            set("L5", "&cRed  vies: &f" + game.livesStringRed());
            set("L6", plugin.color("&7" + plugin.getConfig().getString("scoreboard.sponsorFooter","")));
            set("L7", "");
        }

        for (Player pl : Bukkit.getOnlinePlayers()) if (pl.getScoreboard()!=sb) pl.setScoreboard(sb);
    }
}
