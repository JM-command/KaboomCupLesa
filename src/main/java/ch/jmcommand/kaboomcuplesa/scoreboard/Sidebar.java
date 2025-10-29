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

    // Entrées stables (pas de reset → pas de clignement)
    private final Map<String, Team> lineTeams = new HashMap<>();

    public Sidebar(KaboomCupLesa plugin, GameManager game){
        this.plugin = plugin;
        this.game = game;

        ScoreboardManager sm = Bukkit.getScoreboardManager();
        this.sb = sm.getNewScoreboard();
        this.obj = sb.registerNewObjective(
                "kaboom","dummy",
                plugin.color(plugin.messages().getString("scoreboard.title","&lKaboomCup Lesa"))
        );
        this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        if (game.state()== GameState.LOBBY){
            setText("blueDeaths", plugin.color("&9Blue prêts: &f" + game.teamSizeBlue()));
            setText("redDeaths",  plugin.color("&cRed  prêts: &f" + game.teamSizeRed()));
            setText("blueBlocks", plugin.color("&7Etat: &fLOBBY"));
            setText("redBlocks",  plugin.color("&7Tip: &fClique la boussole pour &e/menu"));
            setText("spacer",     " ");
            setText("state",      plugin.messages().getString("scoreboard.sponsor").replace("{footer}", plugin.getConfig().getString("scoreboard.sponsorFooter")));
            setText("timer",      "");
            return;
        }
        // Lignes fixes (scores discrets 1..7)
        createLine(7, "blueDeaths");
        createLine(6, "redDeaths");
        createLine(5, "blueBlocks");
        createLine(4, "redBlocks");
        createLine(3, "spacer");
        createLine(2, "state");
        createLine(1, "timer");

        // Donner le SB aux joueurs présents
        for (Player pl : Bukkit.getOnlinePlayers()) pl.setScoreboard(sb);

        // Ticker 1/sec
        Bukkit.getScheduler().runTaskTimer(plugin, ()->{
            update();
            game.tickTimeoutCheck();
        }, 0L, 20L);
    }

    private void createLine(int score, String key){
        String entry = "§" + Integer.toHexString(score); // entrée unique "invisible-ish"
        Team team = sb.registerNewTeam("L" + score);
        team.addEntry(entry);
        obj.getScore(entry).setScore(score);
        lineTeams.put(key, team);
    }

    private void setText(String key, String text){
        Team t = lineTeams.get(key);
        if (t == null) return;

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
        String blueDeaths = plugin.messages().getString("scoreboard.blueDeaths")
                .replace("{value}", String.valueOf(game.deaths(TeamColor.BLUE)));
        String redDeaths  = plugin.messages().getString("scoreboard.redDeaths")
                .replace("{value}", String.valueOf(game.deaths(TeamColor.RED)));
        String blueBlocks = plugin.messages().getString("scoreboard.blueBlocks")
                .replace("{value}", String.valueOf(game.blocks(TeamColor.BLUE)));
        String redBlocks  = plugin.messages().getString("scoreboard.redBlocks")
                .replace("{value}", String.valueOf(game.blocks(TeamColor.RED)));

        String stateKey = game.state().name();
        String stateTxt = plugin.messages().getString("states."+stateKey, stateKey);
        String state = plugin.messages().getString("scoreboard.state").replace("{state}", stateTxt);

        int secs = game.secondsLeft();
        String mm = String.format("%02d", secs/60);
        String ss = String.format("%02d", secs%60);
        String timer = (game.state()== GameState.RUNNING && plugin.getConfig().getBoolean("scoreboard.showTimer", true))
                ? plugin.messages().getString("scoreboard.timer").replace("{mm}", mm).replace("{ss}", ss)
                : "";

        setText("blueDeaths", blueDeaths);
        setText("redDeaths",  redDeaths);
        setText("blueBlocks", blueBlocks);
        setText("redBlocks",  redBlocks);
        setText("spacer",     " ");
        setText("state",      state);
        setText("timer",      timer);

        // Appliquer aux nouveaux
        for (Player pl : Bukkit.getOnlinePlayers()) if (pl.getScoreboard()!=sb) pl.setScoreboard(sb);
    }
}
