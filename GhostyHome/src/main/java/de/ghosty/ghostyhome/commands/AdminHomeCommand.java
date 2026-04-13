package de.ghosty.ghostyhome.commands;

import de.ghosty.ghostyhome.GhostyHome;
import de.ghosty.ghostyhome.gui.AdminHomeGUI;
import de.ghosty.ghostyhome.gui.HomeGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeCommand implements CommandExecutor, TabCompleter {

    private final GhostyHome plugin;

    public AdminHomeCommand(GhostyHome plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage(plugin.getLangManager().get("error.player-only"));
            return true;
        }
        if (!admin.hasPermission("ghostyhome.admin")) {
            admin.sendMessage(plugin.getLangManager().getPrefixed("error.no-permission"));
            return true;
        }
        if (args.length == 0) {
            new AdminHomeGUI(plugin, admin).open();
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            admin.sendMessage(plugin.getLangManager().getPrefixed("error.player-not-found", args[0]));
            return true;
        }
        new HomeGUI(plugin, admin, target.getUniqueId(), target.getName()).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (!sender.hasPermission("ghostyhome.admin")) return list;
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers())
                if (p.getName().toLowerCase().startsWith(input)) list.add(p.getName());
        }
        return list;
    }
}
