package me.funtage.morecmd.commands;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class FarmerCommand implements CommandExecutor {

    private final MoreCmd plugin;
    public static final org.bukkit.NamespacedKey FARMER_KEY;

    static {
        FARMER_KEY = new org.bukkit.NamespacedKey("morecmd", "farmer_hoe");
    }

    public FarmerCommand(MoreCmd plugin) {
        this.plugin = plugin;
    }

    public boolean isFarmerOn(Player player) {
        int slot = plugin.getConfig().getInt("farmer.farmer-slot", 8);
        ItemStack hoe = player.getInventory().getItem(slot);
        return hoe != null && hoe.hasItemMeta() &&
                hoe.getItemMeta().getPersistentDataContainer().has(FARMER_KEY, PersistentDataType.BYTE);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission("morecmd.farmer.use")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    MessageManager.getMessage("no-permission")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    MessageManager.getMessage("farmer_usage")));
            return true;
        }

        int slot = plugin.getConfig().getInt("farmer.farmer-slot", 8);
        boolean alreadyOn = isFarmerOn(player);

        if (args[0].equalsIgnoreCase("on")) {

            if (alreadyOn) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        MessageManager.getMessage("farmer_already_on")));
                return true;
            }

            ItemStack current = player.getInventory().getItem(slot);
            if (current != null && current.getType() != Material.AIR) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        MessageManager.getMessage("slot-not-empty")));
                return true;
            }

            // Création de la Farmer Hoe
            ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);
            ItemMeta meta = hoe.getItemMeta();

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("farmer.hoe-name", "&aFarmer Hoe")));

            List<String> lore = plugin.getConfig().getStringList("farmer.hoe-lore");
            meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).toList());

            meta.getPersistentDataContainer().set(FARMER_KEY, PersistentDataType.BYTE, (byte) 1);
            meta.setUnbreakable(true);

            // -------------------------------
            // Ajout de l'enchantement Fortune configurable
            // -------------------------------
            int fortuneLevel = plugin.getConfig().getInt("farmer.fortune-level", 0);
            if (fortuneLevel > 0) {
                Enchantment fortune = Enchantment.getByKey(org.bukkit.NamespacedKey.minecraft("fortune"));
                if (fortune != null) {
                    meta.addEnchant(fortune, fortuneLevel, true);
                }
            }

            hoe.setItemMeta(meta);

            player.getInventory().setItem(slot, hoe);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    MessageManager.getMessage("farmer_enabled")));

        } else if (args[0].equalsIgnoreCase("off")) {

            if (!alreadyOn) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        MessageManager.getMessage("farmer_already_off")));
                return true;
            }

            ItemStack current = player.getInventory().getItem(slot);
            if (current != null && current.hasItemMeta() &&
                    current.getItemMeta().getPersistentDataContainer().has(FARMER_KEY, PersistentDataType.BYTE)) {
                player.getInventory().setItem(slot, null);
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    MessageManager.getMessage("farmer_disabled")));

        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    MessageManager.getMessage("farmer_usage")));
        }

        return true;
    }
}
