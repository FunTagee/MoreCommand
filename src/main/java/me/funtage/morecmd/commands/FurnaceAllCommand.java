package me.funtage.morecmd.commands;

import java.util.HashMap;
import java.util.Map;
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

public class FurnaceAllCommand implements CommandExecutor {
    private final CooldownManager cooldownManager = MoreCmd.getInstance().getCooldownManager();

    public FurnaceAllCommand() {
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            return true;
        } else if (this.cooldownManager.isOnCooldown(p, "furnaceall")) {
            long remaining = this.cooldownManager.getRemaining(p, "furnaceall");
            p.sendMessage(MessageManager.getMessage("furnace_cooldown").replace("%seconds%", String.valueOf(remaining)));
            return true;
        } else {
            int totalSmelted = 0;
            int skipped = 0;
            Map<Material, Integer> resultCounts = new HashMap();

            for(ItemStack item : p.getInventory().getContents()) {
                if (item != null && !item.getType().isAir()) {
                    Material input = item.getType();
                    String key = input.name();
                    if (!MoreCmd.getInstance().getRecipesConfig().contains("recipes." + key)) {
                        skipped += item.getAmount();
                    } else {
                        Material resultMat = Material.valueOf(MoreCmd.getInstance().getRecipesConfig().getString("recipes." + key));
                        int amt = item.getAmount();
                        totalSmelted += amt;
                        resultCounts.put(resultMat, (Integer)resultCounts.getOrDefault(resultMat, 0) + amt);
                        item.setType(resultMat);
                    }
                }
            }

            if (totalSmelted > 0) {
                p.sendMessage(MessageManager.getMessage("furnaceall_success").replace("%count%", String.valueOf(totalSmelted)));
                if (XpManager.isEnabled()) {
                    double totalXp = (double)0.0F;
                    String mode = MoreCmd.getInstance().getConfig().getString("xp-drop.mode", "per-item").toLowerCase();

                    for(Map.Entry<Material, Integer> e : resultCounts.entrySet()) {
                        int amount = (Integer)e.getValue();
                        Material mat = (Material)e.getKey();
                        if ("per-result".equals(mode)) {
                            totalXp += MoreCmd.getInstance().getXpRecipesConfig().getDouble("per-result." + mat.name(), (double)0.0F) * (double)amount;
                        } else {
                            totalXp += MoreCmd.getInstance().getXpRecipesConfig().getDouble("per-item", 0.1) * (double)amount;
                        }
                    }

                    XpManager.giveXp(p, totalXp);
                    String msg = MessageManager.getMessage("xp_received");
                    if (msg != null && !msg.isEmpty()) {
                        p.sendMessage(msg.replace("%xp%", String.valueOf((int)totalXp)));
                    }
                }
            }

            if (skipped > 0) {
                p.sendMessage(MessageManager.getMessage("furnaceall_skipped").replace("%count%", String.valueOf(skipped)));
            }

            if (totalSmelted == 0) {
                p.sendMessage(MessageManager.getMessage("furnaceall_none"));
            }

            int cd = this.cooldownManager.getConfiguredCooldownForPlayer(p, "furnaceall");
            this.cooldownManager.applyCooldown(p, "furnaceall", cd);
            return true;
        }
    }
}
