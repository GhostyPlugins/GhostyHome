package de.ghosty.ghostyhome.manager;

import de.ghosty.ghostyhome.GhostyHome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class HomeManager {

    private final GhostyHome plugin;
    private DatabaseManager dbManager;

    private File              homesFile;
    private FileConfiguration homesConfig;

    private final Map<UUID, Map<String, Location>> homesCache = new HashMap<>();

    public HomeManager(GhostyHome plugin, DatabaseManager dbManager) {
        this.plugin    = plugin;
        this.dbManager = dbManager;
        loadHomes();
    }

    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // ── Load & Save ───────────────────────────────────────────────────────────

    public void loadHomes() {
        homesCache.clear();
        if (dbManager != null) {
            homesCache.putAll(dbManager.loadAll());
            return;
        }
        // YML
        homesFile = new File(plugin.getDataFolder(), "homes.yml");
        if (!homesFile.exists()) {
            try { plugin.getDataFolder().mkdirs(); homesFile.createNewFile(); }
            catch (IOException e) { plugin.getLogger().log(Level.SEVERE, "Could not create homes.yml!", e); }
        }
        homesConfig = YamlConfiguration.loadConfiguration(homesFile);
        if (!homesConfig.contains("players")) return;
        for (String uuidStr : homesConfig.getConfigurationSection("players").getKeys(false)) {
            UUID uuid;
            try { uuid = UUID.fromString(uuidStr); } catch (IllegalArgumentException e) { continue; }
            String basePath = "players." + uuidStr;
            if (homesConfig.getConfigurationSection(basePath) == null) continue;
            Map<String, Location> playerHomes = new HashMap<>();
            for (String homeName : homesConfig.getConfigurationSection(basePath).getKeys(false)) {
                String path = basePath + "." + homeName;
                String worldName = homesConfig.getString(path + ".world");
                if (worldName == null) continue;
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                playerHomes.put(homeName, new Location(world,
                        homesConfig.getDouble(path + ".x"), homesConfig.getDouble(path + ".y"),
                        homesConfig.getDouble(path + ".z"), (float) homesConfig.getDouble(path + ".yaw"),
                        (float) homesConfig.getDouble(path + ".pitch")));
            }
            if (!playerHomes.isEmpty()) homesCache.put(uuid, playerHomes);
        }
    }

    /** No-op in MySQL mode (writes happen per-operation). */
    public void saveHomes() {
        if (dbManager != null) return;
        homesConfig.set("players", null);
        for (Map.Entry<UUID, Map<String, Location>> entry : homesCache.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Location> he : entry.getValue().entrySet()) {
                String path = "players." + uuidStr + "." + he.getKey();
                Location loc = he.getValue();
                homesConfig.set(path + ".world", loc.getWorld().getName());
                homesConfig.set(path + ".x",     loc.getX());
                homesConfig.set(path + ".y",     loc.getY());
                homesConfig.set(path + ".z",     loc.getZ());
                homesConfig.set(path + ".yaw",   loc.getYaw());
                homesConfig.set(path + ".pitch", loc.getPitch());
            }
        }
        try { homesConfig.save(homesFile); }
        catch (IOException e) { plugin.getLogger().log(Level.SEVERE, "Could not save homes.yml!", e); }
    }

    // ── Operations ────────────────────────────────────────────────────────────

    public Map<String, Location> getHomes(UUID uuid) {
        return new HashMap<>(homesCache.getOrDefault(uuid, new HashMap<>()));
    }
    public Map<String, Location> getHomes(Player player) { return getHomes(player.getUniqueId()); }
    public Location getHome(UUID uuid, String name) {
        return homesCache.getOrDefault(uuid, new HashMap<>()).get(name.toLowerCase());
    }
    public Location getHome(Player player, String name) { return getHome(player.getUniqueId(), name); }
    public boolean hasHome(Player player, String name) {
        return homesCache.getOrDefault(player.getUniqueId(), new HashMap<>()).containsKey(name.toLowerCase());
    }

    public boolean setHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        Map<String, Location> playerHomes = homesCache.computeIfAbsent(uuid, k -> new HashMap<>());
        if (!playerHomes.containsKey(name.toLowerCase()) && playerHomes.size() >= getMaxHomes(player))
            return false;
        Location loc = player.getLocation().clone();
        playerHomes.put(name.toLowerCase(), loc);
        if (dbManager != null) dbManager.setHome(uuid, name.toLowerCase(), loc);
        else saveHomes();
        return true;
    }

    public boolean deleteHome(Player player, String name) {
        Map<String, Location> playerHomes = homesCache.getOrDefault(player.getUniqueId(), new HashMap<>());
        if (playerHomes.remove(name.toLowerCase()) == null) return false;
        if (dbManager != null) dbManager.deleteHome(player.getUniqueId(), name.toLowerCase());
        else saveHomes();
        return true;
    }

    public int getMaxHomes(Player player) {
        for (int i = 100; i >= 1; i--)
            if (player.hasPermission("ghostyhome.homes." + i)) return i;
        return plugin.getConfig().getInt("default-max-homes", 5);
    }
    public int getHomeCount(Player player) {
        return homesCache.getOrDefault(player.getUniqueId(), new HashMap<>()).size();
    }
}
