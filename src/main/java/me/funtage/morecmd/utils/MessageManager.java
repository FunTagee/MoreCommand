package me.funtage.morecmd.utils;

import me.funtage.morecmd.MoreCmd;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageManager {

    public static String getMessage(String path) {
        FileConfiguration messagesConfig = MoreCmd.getInstance().getMessagesConfig();

        // Récupère le préfixe depuis le messages.yml
        String prefix = messagesConfig.getString("prefix", "");

        // Récupère le message demandé
        String msg = messagesConfig.getString(path);
        if (msg == null) return ChatColor.translateAlternateColorCodes('&', prefix + "§cMessage not found: " + path);

        // Retourne le message avec le préfixe et les couleurs
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }
}
