package me.funtage.morecmd.managers;

import me.funtage.morecmd.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ZonePurgeManager {

    private final JavaPlugin plugin;
    private final ChunkManager chunkManager;
    private BukkitRunnable purgeTask;

    public ZonePurgeManager(JavaPlugin plugin, ChunkManager chunkManager) {
        this.plugin = plugin;
        this.chunkManager = chunkManager;
    }

    public void startPurgeTask() {
        int intervalSec = plugin.getConfig().getInt("zone-purge.purge-interval", 300);
        int warningDelay = plugin.getConfig().getInt("zone-purge.warning-delay", 30);

        purgeTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfig().isConfigurationSection("zone-purge")) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (String entityName : plugin.getConfig().getConfigurationSection("zone-purge").getKeys(false)) {
                        if (entityName.equalsIgnoreCase("purge-interval") || entityName.equalsIgnoreCase("warning-delay")) continue;

                        Map<?, ?> settings = plugin.getConfig().getConfigurationSection("zone-purge." + entityName).getValues(false);
                        int radius = (int) settings.get("radius");
                        int max = (int) settings.get("max");

                        EntityType type;
                        try {
                            type = EntityType.valueOf(entityName.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("ZonePurge: Unknown entity type " + entityName);
                            continue;
                        }

                        // Liste des entités au moment de l'avertissement
                        List<Entity> nearbyAtWarning = new ArrayList<>();
                        for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
                            if (e.getType() == type) nearbyAtWarning.add(e);
                        }

                        if (nearbyAtWarning.size() > max) {
                            // Message avertissement avec compteur
                            String warningMsg = MessageManager.getMessage("purge-warning")
                                    .replace("%type%", type.name())
                                    .replace("%seconds%", String.valueOf(warningDelay))
                                    .replace("%count%", String.valueOf(nearbyAtWarning.size()))
                                    .replace("%limit%", String.valueOf(max));
                            player.sendMessage(warningMsg);

                            // Tâche différée pour la purge
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    // Recalculer les entités réelles avant de purger
                                    List<Entity> nearbyNow = new ArrayList<>();
                                    for (Entity e : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
                                        if (e.getType() == type) nearbyNow.add(e);
                                    }

                                    int excess = nearbyNow.size() - max;
                                    if (excess <= 0) {
                                        // Message si purge annulée
                                        String cancelMsg = MessageManager.getMessage("purge-cancelled")
                                                .replace("%type%", type.name())
                                                .replace("%count%", String.valueOf(nearbyNow.size()))
                                                .replace("%limit%", String.valueOf(max));
                                        player.sendMessage(cancelMsg);
                                        return;
                                    }

                                    // Supprimer les plus jeunes
                                    nearbyNow.sort(Comparator.comparingInt(Entity::getTicksLived));
                                    for (int i = 0; i < excess; i++) {
                                        nearbyNow.get(i).remove();
                                    }

                                    String doneMsg = MessageManager.getMessage("purge-done")
                                            .replace("%type%", type.name());
                                    player.sendMessage(doneMsg);
                                }
                            }.runTaskLater(plugin, 20L * warningDelay);
                        }
                    }
                }
            }
        };

        purgeTask.runTaskTimer(plugin, 20L * intervalSec, 20L * intervalSec);
    }

    public void cancelPurgeTask() {
        if (purgeTask != null) purgeTask.cancel();
    }
}