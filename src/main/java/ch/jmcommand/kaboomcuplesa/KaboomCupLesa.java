package ch.jmcommand.kaboomcuplesa;

import ch.jmcommand.kaboomcuplesa.command.KaboomCommand;
import ch.jmcommand.kaboomcuplesa.command.MenuCommand;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.listener.*;
import ch.jmcommand.kaboomcuplesa.scoreboard.Sidebar;
import ch.jmcommand.kaboomcuplesa.team.NametagService;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import ch.jmcommand.kaboomcuplesa.ui.TeamMenu;
import ch.jmcommand.kaboomcuplesa.kit.KitService;
import ch.jmcommand.kaboomcuplesa.storage.LeagueStore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class KaboomCupLesa extends JavaPlugin {

    private static KaboomCupLesa instance;

    // === Services / Managers accessibles partout ===
    private TeamManager teams;
    private GameManager game;
    private NametagService tags;
    private TeamMenu menu;
    private Sidebar sidebar;
    private KitService kits;
    private LeagueStore league; // si tu veux garder les points “saison/semaine”

    private FileConfiguration messages;   // messages.yml
    private final String logPrefix = "[KaboomCup] ";

    public static KaboomCupLesa get() { return instance; }

    @Override
    public void onEnable() {
        instance = this;

        // Génère fichiers
        saveDefaultConfig();
        saveResourceIfNotExists("messages.yml");
        loadMessages();

        info("Plugin activé. Version: " + getDescription().getVersion());
        info("Auteur: " + String.join(", ", getDescription().getAuthors()));
        info("Site: " + getDescription().getWebsite());

        // === Wiring (sans ZoneManager / sans WorldEdit) ===
        teams   = new TeamManager(this);
        game    = new GameManager(this, teams, /*zones*/ null); // on passe null, plus de zones
        tags    = new NametagService(this);
        menu    = new TeamMenu(this, teams);
        sidebar = new Sidebar(this, game);
        kits    = new KitService(this);
        league  = new LeagueStore(this); // optionnel

        // === Commands ===
        KaboomCommand kaboomCmd = new KaboomCommand(this, game, teams, /*zones*/ null, tags, kits);
        getCommand("kaboom").setExecutor(kaboomCmd);
        getCommand("kaboom").setTabCompleter(kaboomCmd);
        getCommand("menu").setExecutor(new MenuCommand(menu));

        // === Listeners ===
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(this, /*zones*/ null, teams, tags, kits), this); // donne l’item hub
        pm.registerEvents(new QuitListener(), this);
        pm.registerEvents(new DeathListener(game), this);
        pm.registerEvents(new InventoryListener(menu), this);
        // plus de BlockBreakListener / ExplodeListener pour le scoring
        pm.registerEvents(new HungerVoidListener(this, game), this); // faim infinie + gestion void
        pm.registerEvents(new MenuLockListener(this, game), this);   // bloque /menu en RUNNING
        pm.registerEvents(new HubItemListener(this, game, menu), this); // clic droit item hub

        info("Initialisation terminée.");
    }

    @Override
    public void onDisable() {
        info("Plugin désactivé.");
    }

    /* ==================== CONFIG / MESSAGES ==================== */

    public void reloadAll() {
        reloadConfig();
        loadMessages();
        info("Configuration et messages rechargés.");
    }

    private void loadMessages() {
        File file = new File(getDataFolder(), "messages.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Impossible de charger messages.yml : " + e.getMessage());
        }
        this.messages = cfg;
    }

    private void saveResourceIfNotExists(String name) {
        File f = new File(getDataFolder(), name);
        if (!f.exists()) {
            saveResource(name, false);
        }
    }

    public FileConfiguration messages() { return messages; }

    /* ==================== TEXTE ==================== */

    public String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String msg(String path) {
        String raw = messages().getString(path, "&c<msg:" + path + ">");
        return color(raw);
    }

    public String msg(String path, Map<String, String> placeholders) {
        String out = messages().getString(path, "&c<msg:" + path + ">");
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                out = out.replace("{" + e.getKey() + "}", e.getValue());
            }
        }
        return color(out);
    }

    public String prefixed(String key) {
        return color(messages().getString("prefix", "&eKaboomCup &7» ")) + msg(key);
    }

    /* ==================== LOG ==================== */

    public void info(String s)  { getLogger().info(logPrefix + s); }
    public void warn(String s)  { getLogger().warning(logPrefix + s); }
    public void error(String s) { getLogger().severe(logPrefix + s); }

    /* ==================== Commande /kaboom (fallback simple) ==================== */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("kaboom")) return false;

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("kaboom.admin")) {
                sender.sendMessage(color(messages().getString("errors.noPermission")));
                return true;
            }
            reloadAll();
            sender.sendMessage(color(messages().getString("admin.configReloaded")));
            return true;
        }

        sender.sendMessage(color(messages().getString("help.header")));
        for (String line : messages().getStringList("help.lines")) {
            sender.sendMessage(color(line
                    .replace("{freeze}", String.valueOf(getConfig().getInt("rules.freezeOnStartSeconds", 5)))
            ));
        }
        return true;
    }

    /* ==================== Accès utiles ==================== */

    public String gameWorldName() { return getConfig().getString("world", "tntwars"); }

    public void broadcast(String msgKey) {
        String msg = msg(msgKey);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }

    // Getters pour services si besoin ailleurs
    public TeamManager teams() { return teams; }
    public GameManager game() { return game; }
    public NametagService tags() { return tags; }
    public TeamMenu menu() { return menu; }
    public Sidebar sidebar() { return sidebar; }
    public KitService kits() { return kits; }
    public LeagueStore league() { return league; }
}
