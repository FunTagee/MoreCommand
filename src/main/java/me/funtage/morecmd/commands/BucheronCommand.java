package me.funtage.morecmd.commands;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class BucheronCommand implements CommandExecutor {

    private final MoreCmd plugin;

    public BucheronCommand(MoreCmd plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("morecmd.bucheron.use")) {
            player.sendMessage(MessageManager.getMessage("no-permission"));
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            player.sendMessage(MessageManager.getMessage("lumber_usage"));
            return true;
        }

        String arg = args[0].toLowerCase();

        switch (arg) {
            case "on":
                if (!plugin.getBucheronPlayers().contains(uuid)) {
                    plugin.getBucheronPlayers().add(uuid);
                    player.sendMessage(MessageManager.getMessage("lumber_enabled"));
                } else {
                    player.sendMessage(MessageManager.getMessage("lumber_already_on"));
                }
                break;

            case "off":
                if (plugin.getBucheronPlayers().contains(uuid)) {
                    plugin.getBucheronPlayers().remove(uuid);
                    player.sendMessage(MessageManager.getMessage("lumber_disabled"));
                } else {
                    player.sendMessage(MessageManager.getMessage("lumber_already_off"));
                }
                break;

            default:
                player.sendMessage(MessageManager.getMessage("lumber_usage"));
                break;
        }

        return true;
    }
}
