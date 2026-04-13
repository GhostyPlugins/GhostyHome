package de.ghosty.ghostyhome.commands;

import de.ghosty.ghostyhome.GhostyHome;
import de.ghosty.ghostyhome.gui.HomeGUI;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final GhostyHome plugin;

    public HomeCommand(GhostyHome plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().get("error.player-only"));
            return true;
        }
        if (!player.hasPermission("ghostyhome.home")) {
            player.sendMessage(plugin.getLangManager().getPrefixed("error.no-permission"));
            return true;
        }

        Map<String, Location> homes = plugin.getHomeManager().getHomes(player);

        if (args.length == 0) {
            if (homes.isEmpty()) {
                player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.no-homes"));
                return true;
            }
            if (homes.size() == 1) {
                String name = homes.keySet().iterator().next();
                requestTP(player, name);
                return true;
            }
            new HomeGUI(plugin, player, player.getUniqueId(), player.getName()).open();
            return true;
        }

        String homeName = args[0].toLowerCase();
        if (!plugin.getHomeManager().hasHome(player, homeName)) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.not-found", args[0]));
            return true;
        }
        requestTP(player, homeName);
        return true;
    }

    private void requestTP(Player player, String homeName) {
        Location loc = plugin.getHomeManager().getHome(player, homeName);
        if (loc == null) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.not-found", homeName));
            return;
        }
        plugin.getTeleportManager().requestTeleport(player, loc, homeName, false);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (!(sender instanceof Player player)) return list;
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String name : plugin.getHomeManager().getHomes(player).keySet())
                if (name.startsWith(input)) list.add(name);
        }
        return list;
    }
}
