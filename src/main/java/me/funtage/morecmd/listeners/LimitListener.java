package me.funtage.morecmd.listeners;

import me.funtage.morecmd.MoreCmd;
import me.funtage.morecmd.managers.ChunkManager;
import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;

public class LimitListener implements Listener {

    private final ChunkManager chunkManager;

    public LimitListener(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    // ENTITÉS
    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == SpawnReason.CUSTOM) return;

        var chunk = event.getLocation().getChunk();
        EntityType type = event.getEntityType();

        int limit = MoreCmd.getInstance().getConfig().getInt("entity-limits." + type.name(), -1);
        if (limit == -1) return;

        // Compte réel des entités dans le chunk
        List<Entity> nearby = new ArrayList<>();
        for (Entity e : chunk.getEntities()) {
            if (e.getType() == type) nearby.add(e);
        }

        if (nearby.size() >= limit) {
            event.setCancelled(true);
        } else {
            chunkManager.addEntity(chunk, type);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        chunkManager.removeEntity(event.getEntity().getLocation().getChunk(), event.getEntityType());
    }

    // BLOCS
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var chunk = event.getBlock().getChunk();
        Material mat = event.getBlock().getType();

        int limit = MoreCmd.getInstance().getConfig().getInt("block-limits." + mat.name(), -1);
        if (limit == -1) return;

        int count = chunkManager.getBlockCount(chunk, mat);
        if (count >= limit) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(MessageManager.getMessage("block-limit"));
        } else {
            chunkManager.addBlock(chunk, mat);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        chunkManager.removeBlock(event.getBlock().getChunk(), event.getBlock().getType());
    }
}
