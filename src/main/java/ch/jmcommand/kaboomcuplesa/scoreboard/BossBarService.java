package ch.jmcommand.kaboomcuplesa.scoreboard;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Map;

public class BossBarService {

    private final KaboomCupLesa plugin;
    private final GameManager game;
    private final BossBar bar;

    public BossBarService(KaboomCupLesa plugin, GameManager game) {
        this.plugin = plugin;
        this.game = game;

        // Titre initial depuis messages.yml (clé bossbar.titleInit si tu veux, sinon vide)
        String initialTitle = plugin.msg("bossbar.lobby"); // fallback couleur géré par plugin.msg
        this.bar = Bukkit.createBossBar(initialTitle, BarColor.BLUE, BarStyle.SEGMENTED_12, new BarFlag[0]);
        this.bar.setVisible(true);

        // Tick 1s
        Bukkit.getScheduler().runTaskTimer(plugin, this::update, 0L, 20L);
    }

    public void update() {
        // Audience : on ajoute tous les joueurs connectés
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!bar.getPlayers().contains(p)) bar.addPlayer(p);
        }

        GameState st = game.state();
        if (st == GameState.LOBBY) {
            bar.setTitle(plugin.msg("bossbar.lobby"));
            bar.setColor(BarColor.BLUE);
            bar.setProgress(1.0);
        } else if (st == GameState.PAUSED) {
            bar.setTitle(plugin.msg("bossbar.paused"));
            bar.setColor(BarColor.YELLOW);
            bar.setProgress(1.0);
        } else { // RUNNING
            int secs = game.secondsLeft();
            int total = Math.max(1, plugin.getConfig().getInt("rules.matchDurationSeconds", 1200));
            double prog = Math.max(0.0, Math.min(1.0, (double) secs / (double) total));
            String mm = String.format("%02d", secs / 60);
            String ss = String.format("%02d", secs % 60);

            bar.setTitle(plugin.msg("bossbar.running", Map.of(
                    "mm", mm,
                    "ss", ss
            )));
            bar.setColor(BarColor.GREEN);
            bar.setProgress(prog);
        }
    }

    public void remove(Player p){
        bar.removePlayer(p);
    }
}
