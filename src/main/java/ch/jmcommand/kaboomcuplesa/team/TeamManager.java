package ch.jmcommand.kaboomcuplesa.team;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamManager {

    private final KaboomCupLesa plugin;
    private final Map<UUID, TeamColor> teamOf = new HashMap<>();
    private final Set<UUID> spectators = new HashSet<>();

    public TeamManager(KaboomCupLesa plugin){
        this.plugin = plugin;
    }

    public void join(Player p, TeamColor team){
        teamOf.put(p.getUniqueId(), team);
        spectators.remove(p.getUniqueId());
    }

    public void force(Player p, TeamColor team){
        join(p, team);
        p.sendMessage(plugin.color("&7Tu as été assigné à &l" + (team==TeamColor.BLUE?"&9BLEU":"&cROUGE") + "&7."));
    }

    public boolean isPlaying(Player p){
        return teamOf.containsKey(p.getUniqueId()) && !spectators.contains(p.getUniqueId());
    }

    public boolean isSpectator(Player p){
        return spectators.contains(p.getUniqueId());
    }

    public void setSpectator(Player p){
        spectators.add(p.getUniqueId());
    }

    public TeamColor get(Player p){
        return teamOf.get(p.getUniqueId());
    }

    public List<Player> online(TeamColor team){
        List<Player> out = new ArrayList<>();
        for (UUID id : teamOf.keySet()){
            if (teamOf.get(id) == team){
                Player pl = plugin.getServer().getPlayer(id);
                if (pl != null) out.add(pl);
            }
        }
        return out;
    }

    public int size(TeamColor team){
        return online(team).size();
    }

    public int maxPerTeam(){
        return plugin.getConfig().getInt("rules.maxPlayersPerTeam", 5);
    }

    public boolean isTeamFull(TeamColor t){
        return size(t) >= maxPerTeam();
    }

    public boolean areBalanced(){
        if (!plugin.getConfig().getBoolean("rules.requireTeamsBalanced", true)) return true;
        int b = size(TeamColor.BLUE), r = size(TeamColor.RED);
        return Math.abs(b - r) <= 1;
    }

    public Collection<Player> allOnline(){
        List<Player> out = new ArrayList<>();
        for (UUID id : teamOf.keySet()){
            Player p = plugin.getServer().getPlayer(id);
            if (p!=null) out.add(p);
        }
        return out;
    }
}
