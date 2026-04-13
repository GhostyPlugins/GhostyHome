package de.ghosty.ghostyhome.listener;

import de.ghosty.ghostyhome.GhostyHome;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProtectionListener implements Listener {

    private final GhostyHome plugin;

    public ProtectionListener(GhostyHome plugin) { this.plugin = plugin; }

    /** Any damage (fall, lava, fire, void, explosion, melee, projectile, …). */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        plugin.getTeleportManager().notifyDamage(player.getUniqueId());
    }

    /** PvP damage: registers additional combat cooldown. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (event.getDamager() instanceof Player) {
            plugin.getTeleportManager().notifyCombat(victim.getUniqueId());
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player) {
            plugin.getTeleportManager().notifyCombat(victim.getUniqueId());
        }
    }

    /** Cancel warmup on positional movement. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getTeleportManager().hasWarmup(player.getUniqueId())) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        plugin.getTeleportManager().notifyMove(player.getUniqueId());
    }

    /** Cancel warmup silently when player disconnects. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getTeleportManager().cancelWarmup(event.getPlayer().getUniqueId(), null);
    }
}
