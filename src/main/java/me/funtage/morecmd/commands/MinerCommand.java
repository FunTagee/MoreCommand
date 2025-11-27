package me.funtage.morecmd.commands;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MinerCommand implements CommandExecutor {

    private final MoreCmd plugin;

    public MinerCommand(MoreCmd plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player p = (Player) sender;

        if (args.length != 1) {
            p.sendMessage(MessageManager.getMessage("miner_usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            if (plugin.getMinerPlayers().contains(p.getUniqueId())) {
                p.sendMessage(MessageManager.getMessage("miner_already_on"));
                return true;
            }
            plugin.getMinerPlayers().add(p.getUniqueId());
            p.sendMessage(MessageManager.getMessage("miner_enabled"));

        } else if (args[0].equalsIgnoreCase("off")) {
            if (!plugin.getMinerPlayers().contains(p.getUniqueId())) {
                p.sendMessage(MessageManager.getMessage("miner_already_off"));
                return true;
            }
            plugin.getMinerPlayers().remove(p.getUniqueId());
            p.sendMessage(MessageManager.getMessage("miner_disabled"));

        } else {
            p.sendMessage(MessageManager.getMessage("miner_usage"));
        }

        return true;
    }
}
