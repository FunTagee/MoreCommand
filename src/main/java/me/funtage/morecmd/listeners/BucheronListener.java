package me.funtage.morecmd.listeners;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class BucheronListener implements Listener {

    private final MoreCmd plugin;

    public BucheronListener(MoreCmd plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = (Player) event.getPlayer();

        if (!plugin.getBucheronPlayers().contains(player.getUniqueId())) return;

        Block block = event.getBlock();

        boolean isAllowedLog = block.getType().toString().endsWith("_LOG");
        boolean isAllowedSpecial = plugin.getConfig().getStringList("lumberjack.trees").contains(block.getType().name());
        if (!isAllowedLog && !isAllowedSpecial) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!plugin.getConfig().getStringList("lumberjack.axes").contains(item.getType().name())) {
            player.sendMessage(MessageManager.getMessage("lumber_wrong_tool"));
            return;
        }

        cutTree(block);
        player.sendMessage(MessageManager.getMessage("tree_cut"));
    }

    private void cutTree(Block startBlock) {
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        queue.add(startBlock);
        visited.add(startBlock);

        double maxDistance = 25;

        while (!queue.isEmpty()) {
            Block current = queue.poll();

            boolean isAllowedLog = current.getType().toString().endsWith("_LOG");
            boolean isAllowedSpecial = plugin.getConfig().getStringList("lumberjack.trees").contains(current.getType().name());
            if (!isAllowedLog && !isAllowedSpecial) continue;

            current.breakNaturally();

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block relative = current.getRelative(x, y, z);
                        boolean isRelativeLog = relative.getType().toString().endsWith("_LOG");
                        boolean isRelativeSpecial = plugin.getConfig().getStringList("lumberjack.trees").contains(relative.getType().name());

                        if (!visited.contains(relative) &&
                                (isRelativeLog || isRelativeSpecial) &&
                                relative.getLocation().distance(startBlock.getLocation()) <= maxDistance) {
                            queue.add(relative);
                            visited.add(relative);
                        }
                    }
                }
            }
        }
    }
}
