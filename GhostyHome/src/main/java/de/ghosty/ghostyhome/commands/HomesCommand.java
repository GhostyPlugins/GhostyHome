package de.ghosty.ghostyhome.commands;

import de.ghosty.ghostyhome.GhostyHome;
import de.ghosty.ghostyhome.gui.HomeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomesCommand implements CommandExecutor {

    private final GhostyHome plugin;

    public HomesCommand(GhostyHome plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().get("error.player-only"));
            return true;
        }
        if (!player.hasPermission("ghostyhome.homes")) {
            player.sendMessage(plugin.getLangManager().getPrefixed("error.no-permission"));
            return true;
        }
        if (plugin.getHomeManager().getHomes(player).isEmpty()) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.no-homes"));
            return true;
        }
        new HomeGUI(plugin, player, player.getUniqueId(), player.getName()).open();
        return true;
    }
}
