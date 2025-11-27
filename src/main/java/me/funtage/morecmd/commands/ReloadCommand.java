package me.funtage.morecmd.commands;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("morecmd.reload")) {
            sender.sendMessage(MessageManager.getMessage("no-permission"));
            return true;
        }

        MoreCmd.getInstance().reloadPlugin();
        sender.sendMessage(MessageManager.getMessage("reload-success"));
        return true;
    }
}
