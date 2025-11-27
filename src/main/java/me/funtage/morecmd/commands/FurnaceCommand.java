package me.funtage.morecmd.commands;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.managers.CooldownManager;
import me.funtage.morecmd.managers.XpManager;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FurnaceCommand implements CommandExecutor {
    private final CooldownManager cooldownManager = MoreCmd.getInstance().getCooldownManager();

    public FurnaceCommand() {
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            if (this.cooldownManager.isOnCooldown(p, "furnace")) {
                long remaining = this.cooldownManager.getRemaining(p, "furnace");
                p.sendMessage(MessageManager.getMessage("furnace_cooldown").replace("%seconds%", String.valueOf(remaining)));
                return true;
            } else {
                ItemStack item = p.getInventory().getItemInMainHand();
                if (item != null && !item.getType().isAir()) {
                    Material original = item.getType();
                    String inputName = original.name();
                    if (!MoreCmd.getInstance().getRecipesConfig().contains("recipes." + inputName)) {
                        p.sendMessage(MessageManager.getMessage("not-smeltable").replace("%item%", this.friendlyName(original)));
                        return true;
                    } else {
                        Material resultMat = Material.valueOf(MoreCmd.getInstance().getRecipesConfig().getString("recipes." + inputName));
                        int amount = item.getAmount();
                        item.setAmount(0);
                        p.getInventory().addItem(new ItemStack[]{new ItemStack(resultMat, amount)});
                        p.sendMessage(MessageManager.getMessage("furnace_success").replace("%item%", this.friendlyName(original)).replace("%result%", this.friendlyName(resultMat)).replace("%amount%", String.valueOf(amount)));
                        if (XpManager.isEnabled()) {
                            String mode = MoreCmd.getInstance().getConfig().getString("xp-drop.mode", "per-item").toLowerCase();
                            double xp;
                            if ("per-result".equals(mode)) {
                                xp = MoreCmd.getInstance().getXpRecipesConfig().getDouble("per-result." + resultMat.name(), (double)0.0F) * (double)amount;
                            } else {
                                xp = MoreCmd.getInstance().getXpRecipesConfig().getDouble("per-item", 0.1) * (double)amount;
                            }

                            XpManager.giveXp(p, xp);
                            String msg = MessageManager.getMessage("xp_received");
                            if (msg != null && !msg.isEmpty()) {
                                p.sendMessage(msg.replace("%xp%", String.valueOf((int)xp)));
                            }
                        }

                        int cd = this.cooldownManager.getConfiguredCooldownForPlayer(p, "furnace");
                        this.cooldownManager.applyCooldown(p, "furnace", cd);
                        return true;
                    }
                } else {
                    p.sendMessage(MessageManager.getMessage("no-item-in-hand"));
                    return true;
                }
            }
        } else {
            return true;
        }
    }

    private String friendlyName(Material mat) {
        String name = mat.name().toLowerCase().replace("_", " ");
        char var10000 = Character.toUpperCase(name.charAt(0));
        return var10000 + name.substring(1);
    }
}
