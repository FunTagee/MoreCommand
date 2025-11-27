package me.funtage.morecmd.managers;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {

    private final Map<Chunk, Map<EntityType, Integer>> entityCountMap = new HashMap<>();
    private final Map<Chunk, Map<Material, Integer>> blockCountMap = new HashMap<>();

    // ENTITÉS
    public int getEntityCount(Chunk chunk, EntityType type) {
        return entityCountMap.getOrDefault(chunk, new HashMap<>()).getOrDefault(type, 0);
    }

    public void addEntity(Chunk chunk, EntityType type) {
        entityCountMap.putIfAbsent(chunk, new HashMap<>());
        entityCountMap.get(chunk).put(type, getEntityCount(chunk, type) + 1);
    }

    public void removeEntity(Chunk chunk, EntityType type) {
        if (!entityCountMap.containsKey(chunk)) return;
        Map<EntityType, Integer> map = entityCountMap.get(chunk);
        map.put(type, Math.max(0, map.getOrDefault(type, 1) - 1));
    }

    // BLOCS
    public int getBlockCount(Chunk chunk, Material material) {
        return blockCountMap.getOrDefault(chunk, new HashMap<>()).getOrDefault(material, 0);
    }

    public void addBlock(Chunk chunk, Material material) {
        blockCountMap.putIfAbsent(chunk, new HashMap<>());
        blockCountMap.get(chunk).put(material, getBlockCount(chunk, material) + 1);
    }

    public void removeBlock(Chunk chunk, Material material) {
        if (!blockCountMap.containsKey(chunk)) return;
        Map<Material, Integer> map = blockCountMap.get(chunk);
        map.put(material, Math.max(0, map.getOrDefault(material, 1) - 1));
    }

    public void resetChunk(Chunk chunk) {
        entityCountMap.remove(chunk);
        blockCountMap.remove(chunk);
    }
}
