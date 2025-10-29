package ch.jmcommand.kaboomcuplesa.team;

import ch.jmcommand.kaboomcuplesa.KaboomCupLesa;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class NametagService {

    private final KaboomCupLesa plugin;
    private final Scoreboard sb;
    private final Team blueTeam;
    private final Team redTeam;

    public NametagService(KaboomCupLesa p){
        this.plugin = p;
        ScoreboardManager sm = Bukkit.getScoreboardManager();
        this.sb = sm.getMainScoreboard(); // main → compatible TAB plugin

        Team bt = sb.getTeam("KAB_BLUE");
        if (bt==null) bt = sb.registerNewTeam("KAB_BLUE");
        bt.setColor(ChatColor.BLUE);
        bt.setCanSeeFriendlyInvisibles(false);
        bt.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        this.blueTeam = bt;

        Team rt = sb.getTeam("KAB_RED");
        if (rt==null) rt = sb.registerNewTeam("KAB_RED");
        rt.setColor(ChatColor.RED);
        rt.setCanSeeFriendlyInvisibles(false);
        rt.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        this.redTeam = rt;
    }

    public void apply(Player p, TeamColor color){
        // retire d'abord
        blueTeam.removeEntry(p.getName());
        redTeam.removeEntry(p.getName());

        if (color==TeamColor.BLUE){
            blueTeam.addEntry(p.getName());
        } else if (color==TeamColor.RED){
            redTeam.addEntry(p.getName());
        }
        // fallback tablist si besoin
        p.setPlayerListName((color==TeamColor.BLUE? ChatColor.BLUE: ChatColor.RED) + p.getName());
    }

    public void clear(Player p){
        blueTeam.removeEntry(p.getName());
        redTeam.removeEntry(p.getName());
        p.setPlayerListName(p.getName());
    }
}
