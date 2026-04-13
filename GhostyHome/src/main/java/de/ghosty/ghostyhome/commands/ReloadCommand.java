package de.ghosty.ghostyhome.commands;

import de.ghosty.ghostyhome.GhostyHome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final GhostyHome plugin;

    public ReloadCommand(GhostyHome plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ghostyhome.reload")) {
            sender.sendMessage(plugin.getLangManager().getPrefixed("error.no-permission"));
            return true;
        }
        plugin.reloadPlugin();
        sender.sendMessage(plugin.getLangManager().getPrefixed("cmd.reload.success"));
        return true;
    }
}
