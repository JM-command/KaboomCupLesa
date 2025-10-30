package ch.jmcommand.kaboomcuplesa.scoreboard;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Bossbar simple qui affiche l’état du match + timer.
 * - en LOBBY: "KaboomCup Lesa - en attente"
 * - en RUNNING: "Match en cours - mm:ss"
 */
public class BossBarService {

    private final KaboomCupLesa plugin;
    private final GameManager game;
    private final BossBar bar;
    private final Set<Player> watching = new HashSet<>();

    public BossBarService(KaboomCupLesa plugin, GameManager game) {
        this.plugin = plugin;
        this.game = game;
        this.bar = Bukkit.createBossBar(
                plugin.color("&eKaboomCup &7» &fEn attente…"),
                BarColor.PURPLE,
                BarStyle.SOLID
        );
        this.bar.setVisible(true);

        // update chaque seconde
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void add(Player p) {
        watching.add(p);
        bar.addPlayer(p);
    }

    public void remove(Player p) {
        watching.remove(p);
        bar.removePlayer(p);
    }

    private void tick() {
        GameState state = game.state();
        switch (state) {
            case LOBBY -> {
                bar.setTitle(plugin.color("&eKaboomCup &7» &fLobby"));
                bar.setProgress(1.0);
            }
            case PAUSED -> {
                bar.setTitle(plugin.color("&eKaboomCup &7» &6PAUSE"));
                bar.setProgress(1.0);
            }
            case RUNNING -> {
                int secs = game.secondsLeft();
                int mm = secs / 60;
                int ss = secs % 60;
                bar.setTitle(plugin.color("&cMatch en cours &7» &f%02d:%02d".formatted(mm, ss)));
                // petit ratio de temps restant si tu veux
                int total = plugin.getConfig().getInt("rules.matchDurationSeconds", 1200);
                double prog = total > 0 ? Math.max(0, Math.min(1, secs / (double) total)) : 1.0;
                bar.setProgress(prog);
            }
        }
    }
}
