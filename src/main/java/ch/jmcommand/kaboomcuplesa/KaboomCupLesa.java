package ch.jmcommand.kaboomcuplesa;

import ch.jmcommand.kaboomcuplesa.listener.*;
import ch.jmcommand.kaboomcuplesa.zone.ZoneManager;
import ch.jmcommand.kaboomcuplesa.team.TeamManager;
import ch.jmcommand.kaboomcuplesa.team.NametagService;
import ch.jmcommand.kaboomcuplesa.game.GameManager;
import ch.jmcommand.kaboomcuplesa.ui.TeamMenu;
import ch.jmcommand.kaboomcuplesa.scoreboard.Sidebar;
import ch.jmcommand.kaboomcuplesa.storage.LeagueStore;

import ch.jmcommand.kaboomcuplesa.command.KaboomCommand;
import ch.jmcommand.kaboomcuplesa.command.MenuCommand;

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

    private FileConfiguration messages;   // messages.yml
    private String logPrefix = "[KaboomCup] ";

    public static KaboomCupLesa get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Génère config.yml si absent
        saveDefaultConfig();

        // Génère messages.yml si absent
        saveResourceIfNotExists("messages.yml");

        // Charge messages.yml
        loadMessages();

        // Log de démarrage
        info("Plugin activé. Version: " + getDescription().getVersion());
        info("Auteur: " + String.join(", ", getDescription().getAuthors()));
        info("Site: " + getDescription().getWebsite());

        // === WIRING (instances propres) ===
        ZoneManager zones   = new ZoneManager(this);
        TeamManager teams   = new TeamManager(this);
        GameManager game    = new GameManager(this, teams, zones);
        NametagService tags = new NametagService(this);
        TeamMenu menu       = new TeamMenu(this, teams);
        Sidebar side        = new Sidebar(this, game);
        LeagueStore league  = new LeagueStore(this); // optionnel, pour gérer points semaine

        // Commands
        KaboomCommand kaboomCmd = new KaboomCommand(this, game, teams, zones, tags);
        getCommand("kaboom").setExecutor(kaboomCmd);
        getCommand("kaboom").setTabCompleter(kaboomCmd);
        getCommand("menu").setExecutor(new MenuCommand(menu));

        // Listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(this, zones, teams, tags), this);
        pm.registerEvents(new QuitListener(), this);
        pm.registerEvents(new DeathListener(game), this);
        pm.registerEvents(new BlockBreakListener(), this); // désactivé pour le scoring (on compte via TNT)
        pm.registerEvents(new InventoryListener(menu), this);
        pm.registerEvents(new ExplodeListener(teams, game), this);

    }

    @Override
    public void onDisable() {
        info("Plugin désactivé.");
    }

    /* ---------------------------------------------------------
     * Helpers CONFIG / MESSAGES
     * --------------------------------------------------------- */

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

    public FileConfiguration messages() {
        return messages;
    }

    /* ---------------------------------------------------------
     * Helpers TEXTE
     * --------------------------------------------------------- */

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

    /* ---------------------------------------------------------
     * Log helpers
     * --------------------------------------------------------- */

    public void info(String s) {
        getLogger().info(logPrefix + s);
    }

    public void warn(String s) {
        getLogger().warning(logPrefix + s);
    }

    public void error(String s) {
        getLogger().severe(logPrefix + s);
    }

    /* ---------------------------------------------------------
     * Commande /kaboom (temporaire)
     * Pour l’instant: juste /kaboom reload
     * On branchera plus tard sur la vraie KaboomCommand
     * --------------------------------------------------------- */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // On ne capte que la commande "kaboom" déclarée dans plugin.yml
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

        // Afficher une aide minimale (vraie aide dans messages.yml)
        sender.sendMessage(color(messages().getString("help.header")));
        for (String line : messages().getStringList("help.lines")) {
            sender.sendMessage(color(line
                    .replace("{freeze}", String.valueOf(getConfig().getInt("rules.freezeOnStartSeconds", 5)))
            ));
        }
        return true;
    }

    /* ---------------------------------------------------------
     * Méthodes futures (stubs) – on branchera plus tard
     * --------------------------------------------------------- */

    // private void registerCommands() { ... }
    // private void registerListeners() { ... }

    // Exemple d’accès monde depuis config (quand on codera ZoneManager)
    public String gameWorldName() {
        return getConfig().getString("world", "tntwars");
    }

    // Exemple de broadcast avec préfixe
    public void broadcast(String msgKey) {
        String msg = msg(msgKey);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }
}
