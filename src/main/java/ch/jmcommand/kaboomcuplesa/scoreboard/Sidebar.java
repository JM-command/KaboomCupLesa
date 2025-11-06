package ch.jmcommand.kaboomcuplesa.scoreboard;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
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

        // on fait 8 lignes max
        createLine(8, "L0");
        createLine(7, "L1");
        createLine(6, "L2");
        createLine(5, "L3");
        createLine(4, "L4");
        createLine(3, "L5");
        createLine(2, "L6");
        createLine(1, "L7");

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.setScoreboard(sb);
        }

        // update chaque seconde
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

        int week = (plugin.league() != null) ? plugin.league().getWeek() : 1;
        String leaderLine = game.leadingTeamPointsLine(); // ← on réutilise le helper

        // header
        set("L0", "&e&lKaboomCup");
        set("L1", "&7──────────");

        if (game.state() == GameState.LOBBY || game.state() == GameState.PAUSED) {
            // bloc ligue
            set("L2", "&fSemaine &e#" + week);
            set("L3", leaderLine);

            // état
            set("L4", "&7État: &6" + game.state().name());

            // joueurs
            set("L5", "&9Blue ❤: &f" + game.livesStringBlue());
            set("L6", "&cRed  ❤: &f" + game.livesStringRed());

            // tip + footer
            String footer = plugin.getConfig().getString("scoreboard.sponsorFooter", "");
            set("L7", footer.isEmpty() ? "&7» &fClique la boussole" : plugin.color("&8" + footer));

        } else {
            // match en cours
            int secs = game.secondsLeft();
            String mm = String.format("%02d", secs / 60);
            String ss = String.format("%02d", secs % 60);

            set("L2", "&fSemaine &e#" + week);
            set("L3", leaderLine);
            set("L4", "&7État: &aEN JEU");

            if (plugin.getConfig().getBoolean("scoreboard.showTimer", true)) {
                set("L5", "&7Timer: &f" + mm + ":" + ss);
            } else {
                set("L5", "");
            }

            set("L6", "&9Blue ❤: &f" + game.livesStringBlue());
            set("L7", "&cRed  ❤: &f" + game.livesStringRed());
        }

        // appliquer au cas où des joueurs se co après
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (pl.getScoreboard() != sb) {
                pl.setScoreboard(sb);
            }
        }
    }
}
