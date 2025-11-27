package me.funtage.morecmd.listeners;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MinerListener implements Listener {

    private final MoreCmd plugin;
    private final Map<UUID, Long> minerCooldowns = new HashMap<>();

    public MinerListener(MoreCmd plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();

        // Vérifier si le joueur a activé le mode Miner
        if (!plugin.getMinerPlayers().contains(p.getUniqueId())) return;

        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool == null) return;

        // Vérifier si l'outil est autorisé via config
        List<String> allowedTools = plugin.getConfig().getStringList("miner.allowed-tools");
        if (allowedTools == null || allowedTools.isEmpty()) return;
        if (!allowedTools.contains(tool.getType().name())) {
            p.sendMessage(MessageManager.getMessage("miner_wrong_tool"));
            return;
        }

        // Cooldown joueur
        long now = System.currentTimeMillis();
        int cooldownSeconds = plugin.getConfig().getInt("miner.break-cooldown-seconds", 5);
        if (minerCooldowns.containsKey(p.getUniqueId())) {
            long lastBreak = minerCooldowns.get(p.getUniqueId());
            long remainingMs = (lastBreak + cooldownSeconds * 1000L) - now;
            if (remainingMs > 0) {
                long remainingSec = (remainingMs + 999) / 1000; // arrondi supérieur
                p.sendMessage(MessageManager.getMessage("miner_cooldown").replace("%time%", String.valueOf(remainingSec)));
                event.setCancelled(true);
                return;
            }
        }
        minerCooldowns.put(p.getUniqueId(), now);

        Block block = event.getBlock();
        Material type = block.getType();

        // Vérifier si le minerai est autorisé via config
        List<String> allowedOres = plugin.getConfig().getStringList("miner.allowed-ores");
        if (allowedOres == null || allowedOres.isEmpty()) return;
        if (!allowedOres.contains(type.name())) return;

        // Annuler l'événement original pour gérer nous-même
        event.setDropItems(false);
        event.setCancelled(true);

        veinMine(block, p, type);
    }

    private void veinMine(Block start, Player p, Material type) {
        int max = plugin.getConfig().getInt("miner.max-blocks", 128);
        int count = 0;
        int delay = 2; // delay fixe en ticks entre chaque bloc, modifiable ici

        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(start);

        while (!queue.isEmpty()) {
            Block b = queue.poll();
            if (visited.contains(b)) continue;
            visited.add(b);

            if (b.getType() == type) {
                if (count >= max) return;
                count++;

                final Block blockToBreak = b;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    blockToBreak.breakNaturally(p.getInventory().getItemInMainHand());
                }, delay * count);

                // Vérifier les 6 blocs voisins
                for (Block relative : Arrays.asList(
                        b.getRelative(1,0,0), b.getRelative(-1,0,0),
                        b.getRelative(0,1,0), b.getRelative(0,-1,0),
                        b.getRelative(0,0,1), b.getRelative(0,0,-1)
                )) {
                    if (!visited.contains(relative) && relative.getType() == type) {
                        queue.add(relative);
                    }
                }
            }
        }
    }
}