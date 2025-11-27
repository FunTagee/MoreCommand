package me.funtage.morecmd.listeners;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.commands.FarmerCommand;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FarmerListener implements Listener {

    private final FarmerCommand farmerCommand;
    private final MoreCmd plugin;

    public FarmerListener(FarmerCommand farmerCommand, MoreCmd plugin) {
        this.farmerCommand = farmerCommand;
        this.plugin = plugin;
    }

    private boolean isFarmerHoe(ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(FarmerCommand.FARMER_KEY, PersistentDataType.BYTE);
    }

    // --------------------------
    // Bloquer déplacement et drop de la Farmer Hoe
    // --------------------------
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (isFarmerHoe(current) || isFarmerHoe(cursor)) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP) {
            if (isFarmerHoe(event.getView().getBottomInventory().getItem(event.getHotbarButton()))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        event.getNewItems().forEach((slot, item) -> {
            if (isFarmerHoe(item)) event.setCancelled(true);
        });
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isFarmerHoe(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    // --------------------------
    // Bloquer certaines commandes avec la Farmer Hoe
    // --------------------------
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        ItemStack inHand = player.getInventory().getItemInMainHand();

        if (!isFarmerHoe(inHand)) return;

        List<String> blockedCommands = plugin.getConfig().getStringList("farmer.blocked-commands");
        String msg = event.getMessage().substring(1).toLowerCase(); // enlever le '/'

        for (String blocked : blockedCommands) {
            blocked = blocked.toLowerCase();
            if (msg.startsWith(blocked)) {
                event.setCancelled(true);
                player.sendMessage(MessageManager.getMessage("farmer_command_blocked"));
                return;
            }
        }
    }

    // --------------------------
    // Gestion du block break / replant
    // --------------------------
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!farmerCommand.isFarmerOn(player)) return;

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!isFarmerHoe(inHand)) return;

        Block block = event.getBlock();
        Material blockType = block.getType();

        List<String> allowed = plugin.getConfig().getStringList("farmer.allowed-crops");
        if (!allowed.contains(blockType.name())) return;

        event.setCancelled(true); // on gère nous-mêmes la casse

        boolean isMature = false;
        if (block.getBlockData() instanceof Ageable ageable) {
            isMature = ageable.getAge() >= ageable.getMaximumAge();
        } else if (blockType == Material.NETHER_WART) {
            isMature = true;
        }

        if (!isMature) return; // immature → ne rien faire

        // ✅ Casser instantanément comme en vanilla
        block.breakNaturally(inHand);

        // Replantation après 40 ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                Material seed = switch (blockType) {
                    case WHEAT -> Material.WHEAT_SEEDS;
                    case CARROTS -> Material.CARROT;
                    case POTATOES -> Material.POTATO;
                    case BEETROOTS -> Material.BEETROOT_SEEDS;
                    case NETHER_WART -> Material.NETHER_WART;
                    default -> null;
                };

                if (seed == null) return;
                if (!player.getInventory().contains(seed)) return;

                player.getInventory().removeItem(new ItemStack(seed, 1));

                block.setType(blockType, false);
                if (block.getBlockData() instanceof Ageable ageable) {
                    ageable.setAge(0);
                    block.setBlockData(ageable, false);
                }
            }
        }.runTaskLater(plugin, 40L);
    }
}
