package de.ghosty.ghostyhome.commands;

import de.ghosty.ghostyhome.GhostyHome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DelHomeCommand implements CommandExecutor, TabCompleter {

    private final GhostyHome plugin;

    public DelHomeCommand(GhostyHome plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().get("error.player-only"));
            return true;
        }
        if (!player.hasPermission("ghostyhome.delhome")) {
            player.sendMessage(plugin.getLangManager().getPrefixed("error.no-permission"));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.delhome.usage"));
            return true;
        }
        String homeName = args[0].toLowerCase();
        if (!plugin.getHomeManager().hasHome(player, homeName)) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.home.not-found", args[0]));
            return true;
        }
        plugin.getHomeManager().deleteHome(player, homeName);
        player.sendMessage(plugin.getLangManager().getPrefixed("cmd.delhome.success", capitalize(homeName)));
        return true;
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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
