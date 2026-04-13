package de.ghosty.ghostyhome.commands;

import de.ghosty.ghostyhome.GhostyHome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {

    private final GhostyHome plugin;
    private static final int MAX_NAME_LENGTH = 24;

    public SetHomeCommand(GhostyHome plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLangManager().get("error.player-only"));
            return true;
        }
        if (!player.hasPermission("ghostyhome.sethome")) {
            player.sendMessage(plugin.getLangManager().getPrefixed("error.no-permission"));
            return true;
        }
        String homeName = args.length > 0 ? args[0] : "home";
        if (homeName.length() > MAX_NAME_LENGTH) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.sethome.name-too-long", MAX_NAME_LENGTH));
            return true;
        }
        if (!homeName.matches("[a-zA-Z0-9_\\-]+")) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.sethome.invalid-name"));
            return true;
        }
        boolean overwrite = plugin.getHomeManager().hasHome(player, homeName);
        boolean success   = plugin.getHomeManager().setHome(player, homeName);
        if (!success) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.sethome.max-reached",
                    plugin.getHomeManager().getMaxHomes(player)));
        } else if (overwrite) {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.sethome.updated", capitalize(homeName)));
        } else {
            player.sendMessage(plugin.getLangManager().getPrefixed("cmd.sethome.success", capitalize(homeName)));
        }
        return true;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
