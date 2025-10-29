package ch.jmcommand.kaboomcuplesa.game;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.scheduler.BukkitTask;

public class MatchTimer {
    private final KaboomCupLesa plugin;
    private long endAtMs = 0L;
    private BukkitTask task;

    public MatchTimer(KaboomCupLesa plugin){
        this.plugin = plugin;
    }

    public void startSeconds(int seconds){
        stop();
        endAtMs = System.currentTimeMillis() + (seconds * 1000L);
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, ()->{
            // Ticking handled by GameManager update (hooké par Sidebar après)
        }, 0L, 20L);
    }

    public void stop(){
        if (task != null){
            task.cancel();
            task = null;
        }
        endAtMs = 0L;
    }

    public int secondsLeft(){
        if (endAtMs <= 0) return 0;
        long left = (endAtMs - System.currentTimeMillis()) / 1000L;
        return (int)Math.max(0, left);
    }

    public boolean isOver(){
        return secondsLeft() <= 0;
    }
}
