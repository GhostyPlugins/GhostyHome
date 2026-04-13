package de.ghosty.ghostyhome.manager;

import de.ghosty.ghostyhome.GhostyHome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DatabaseManager {

    private final GhostyHome plugin;
    private Connection connection;

    public DatabaseManager(GhostyHome plugin) { this.plugin = plugin; }

    // ── Connection ────────────────────────────────────────────────────────────

    public boolean connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String host     = plugin.getConfig().getString("mysql.host", "localhost");
            int    port     = plugin.getConfig().getInt("mysql.port", 3306);
            String database = plugin.getConfig().getString("mysql.database", "ghostyhome");
            String username = plugin.getConfig().getString("mysql.username", "root");
            String password = plugin.getConfig().getString("mysql.password", "");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&autoReconnect=true&characterEncoding=utf8mb4&serverTimezone=UTC";
            connection = DriverManager.getConnection(url, username, password);
            createTable();
            plugin.getLogger().info(plugin.getLangManager().get("cmd.storage.mysql-connected"));
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, plugin.getLangManager().get("cmd.storage.mysql-failed"), e);
            return false;
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info(plugin.getLangManager().get("cmd.storage.mysql-disconnected"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing MySQL connection.", e);
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) connect();
        return connection;
    }

    // ── Schema ────────────────────────────────────────────────────────────────

    private void createTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS ghostyhome_homes ("
                    + "uuid VARCHAR(36) NOT NULL,"
                    + "home_name VARCHAR(64) NOT NULL,"
                    + "world VARCHAR(64) NOT NULL,"
                    + "x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL,"
                    + "yaw FLOAT NOT NULL, pitch FLOAT NOT NULL,"
                    + "PRIMARY KEY (uuid, home_name)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public Map<UUID, Map<String, Location>> loadAll() {
        Map<UUID, Map<String, Location>> result = new HashMap<>();
        try (PreparedStatement ps = getConnection()
                .prepareStatement("SELECT uuid,home_name,world,x,y,z,yaw,pitch FROM ghostyhome_homes");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID uuid;
                try { uuid = UUID.fromString(rs.getString("uuid")); }
                catch (IllegalArgumentException e) { continue; }
                World world = Bukkit.getWorld(rs.getString("world"));
                if (world == null) continue;
                Location loc = new Location(world,
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch"));
                result.computeIfAbsent(uuid, k -> new HashMap<>())
                      .put(rs.getString("home_name"), loc);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load homes from MySQL!", e);
        }
        return result;
    }

    public void setHome(UUID uuid, String name, Location loc) {
        String sql = "INSERT INTO ghostyhome_homes (uuid,home_name,world,x,y,z,yaw,pitch) "
                + "VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE "
                + "world=VALUES(world),x=VALUES(x),y=VALUES(y),z=VALUES(z),"
                + "yaw=VALUES(yaw),pitch=VALUES(pitch)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setString(3, loc.getWorld().getName());
            ps.setDouble(4, loc.getX());  ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());  ps.setFloat(7, loc.getYaw());
            ps.setFloat(8, loc.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save home to MySQL!", e);
        }
    }

    public void deleteHome(UUID uuid, String name) {
        try (PreparedStatement ps = getConnection()
                .prepareStatement("DELETE FROM ghostyhome_homes WHERE uuid=? AND home_name=?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete home from MySQL!", e);
        }
    }
}
