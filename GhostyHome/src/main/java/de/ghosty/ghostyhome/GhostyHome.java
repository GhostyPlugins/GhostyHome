package de.ghosty.ghostyhome;

import de.ghosty.ghostyhome.commands.*;
import de.ghosty.ghostyhome.listener.GUIListener;
import de.ghosty.ghostyhome.listener.ProtectionListener;
import de.ghosty.ghostyhome.manager.DatabaseManager;
import de.ghosty.ghostyhome.manager.HomeManager;
import de.ghosty.ghostyhome.manager.LangManager;
import de.ghosty.ghostyhome.manager.TeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GhostyHome extends JavaPlugin {

    private static GhostyHome instance;

    private LangManager     langManager;
    private HomeManager     homeManager;
    private TeleportManager teleportManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        langManager     = new LangManager(this);
        teleportManager = new TeleportManager(this);

        // Storage
        String storageType = getConfig().getString("storage", "yml").toLowerCase();
        if (storageType.equals("mysql")) {
            databaseManager = new DatabaseManager(this);
            if (!databaseManager.connect()) {
                getLogger().warning("MySQL failed – falling back to YML storage.");
                databaseManager = null;
            }
        }

        homeManager = new HomeManager(this, databaseManager);

        // Commands
        HomeCommand homeCmd = new HomeCommand(this);
        getCommand("home").setExecutor(homeCmd);
        getCommand("home").setTabCompleter(homeCmd);

        getCommand("homes").setExecutor(new HomesCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));

        DelHomeCommand delCmd = new DelHomeCommand(this);
        getCommand("delhome").setExecutor(delCmd);
        getCommand("delhome").setTabCompleter(delCmd);

        AdminHomeCommand adminCmd = new AdminHomeCommand(this);
        getCommand("adminhome").setExecutor(adminCmd);
        getCommand("adminhome").setTabCompleter(adminCmd);

        getCommand("ghreload").setExecutor(new ReloadCommand(this));

        // Listeners
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);

        getLogger().info("GhostyHome v" + getDescription().getVersion() + " gestartet! "
                + "Storage: " + (databaseManager != null ? "MySQL" : "YML"));
    }

    @Override
    public void onDisable() {
        if (homeManager != null)    homeManager.saveHomes();
        if (databaseManager != null) databaseManager.disconnect();
        getLogger().info("GhostyHome wurde deaktiviert.");
    }

    public void reloadPlugin() {
        reloadConfig();
        langManager.reload();
        teleportManager.loadConfig();

        String storageType = getConfig().getString("storage", "yml").toLowerCase();
        if (storageType.equals("mysql") && databaseManager == null) {
            databaseManager = new DatabaseManager(this);
            if (!databaseManager.connect()) databaseManager = null;
            homeManager.setDatabaseManager(databaseManager);
        } else if (!storageType.equals("mysql") && databaseManager != null) {
            databaseManager.disconnect();
            databaseManager = null;
            homeManager.setDatabaseManager(null);
        }
        homeManager.loadHomes();
    }

    public static GhostyHome getInstance()     { return instance; }
    public HomeManager     getHomeManager()    { return homeManager; }
    public LangManager     getLangManager()    { return langManager; }
    public TeleportManager getTeleportManager(){ return teleportManager; }
    public DatabaseManager getDatabaseManager(){ return databaseManager; }
}
