package de.ghosty.ghostyhome.manager;

import de.ghosty.ghostyhome.GhostyHome;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final GhostyHome plugin;

    private final Map<UUID, Long>       damageTimes     = new HashMap<>();
    private final Map<UUID, Long>       combatTimes     = new HashMap<>();
    private final Map<UUID, BukkitTask> warmupTasks     = new HashMap<>();
    private final Map<UUID, Location>   warmupLocations = new HashMap<>();

    private int     warmupSeconds;
    private boolean cancelOnMove;
    private boolean cancelOnDamage;
    private int     damageCooldownSec;
    private int     combatCooldownSec;

    public TeleportManager(GhostyHome plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        warmupSeconds     = plugin.getConfig().getInt("teleport.warmup", 3);
        cancelOnMove      = plugin.getConfig().getBoolean("teleport.cancel-on-move", true);
        cancelOnDamage    = plugin.getConfig().getBoolean("teleport.cancel-on-damage", true);
        damageCooldownSec = plugin.getConfig().getInt("teleport.damage-cooldown", 5);
        combatCooldownSec = plugin.getConfig().getInt("teleport.combat-cooldown", 10);
    }

    // ── Notifications (from ProtectionListener) ───────────────────────────────

    public void notifyDamage(UUID uuid) {
        damageTimes.put(uuid, System.currentTimeMillis());
        if (cancelOnDamage) cancelWarmup(uuid, "damage");
    }

    public void notifyCombat(UUID uuid) {
        combatTimes.put(uuid, System.currentTimeMillis());
        if (cancelOnDamage) cancelWarmup(uuid, "damage");
    }

    public void notifyMove(UUID uuid) {
        if (cancelOnMove && warmupTasks.containsKey(uuid)) cancelWarmup(uuid, "move");
    }

    // ── Teleport request ──────────────────────────────────────────────────────

    public void requestTeleport(Player player, Location target, String homeName, boolean isAdmin) {
        // Admins always teleport instantly with no cooldown checks
        if (isAdmin || warmupSeconds <= 0) {
            execute(player, target, homeName);
            return;
        }

        long now = System.currentTimeMillis();

        long dmg = remaining(damageTimes.get(player.getUniqueId()), damageCooldownSec, now);
        if (dmg > 0) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.teleport.damage-cooldown", ceil(dmg)));
            return;
        }
        long cbt = remaining(combatTimes.get(player.getUniqueId()), combatCooldownSec, now);
        if (cbt > 0) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.teleport.combat-cooldown", ceil(cbt)));
            return;
        }

        cancelWarmup(player.getUniqueId(), null);

        player.sendMessage(plugin.getLangManager().getPrefixed(
                "cmd.teleport.warmup", warmupSeconds, capitalize(homeName)));

        warmupLocations.put(player.getUniqueId(), player.getLocation().clone());

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            warmupTasks.remove(player.getUniqueId());
            warmupLocations.remove(player.getUniqueId());
            execute(player, target, homeName);
        }, warmupSeconds * 20L);

        warmupTasks.put(player.getUniqueId(), task);
    }

    private void execute(Player player, Location target, String homeName) {
        player.teleport(target);
        player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.teleported", capitalize(homeName)));
    }

    // ── Warmup helpers ────────────────────────────────────────────────────────

    public void cancelWarmup(UUID uuid, String reason) {
        BukkitTask task = warmupTasks.remove(uuid);
        warmupLocations.remove(uuid);
        if (task == null) return;
        task.cancel();
        if (reason == null) return;
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return;
        String key = reason.equals("move") ? "cmd.teleport.cancelled-move" : "cmd.teleport.cancelled-damage";
        player.sendMessage(plugin.getLangManager().getPrefixed(key));
    }

    public boolean hasWarmup(UUID uuid) { return warmupTasks.containsKey(uuid); }
    public Location getWarmupLocation(UUID uuid) { return warmupLocations.get(uuid); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long remaining(Long timestamp, int cooldownSec, long now) {
        if (timestamp == null) return 0;
        return Math.max(0, (timestamp + cooldownSec * 1000L) - now);
    }
    private int ceil(long ms) { return (int) Math.ceil(ms / 1000.0); }
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
