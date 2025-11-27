package me.funtage.morecmd.managers;

import me.funtage.morecmd.MoreCmd;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final MoreCmd plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(MoreCmd plugin) {
        this.plugin = plugin;
    }

    /**
     * Retourne le cooldown configuré pour le joueur selon son groupe/config
     */
    public int getConfiguredCooldownForPlayer(Player p, String base) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("cooldowns." + base);
        if (sec == null) return 60; // fallback si config manquante

        int bestCooldown = sec.getInt("default", 60);

        for (String key : sec.getKeys(false)) {
            if (key.equalsIgnoreCase("default")) continue;

            String perm = "morecmd." + base + "." + key;
            // Si le joueur a la permission du groupe ou la permission globale *
            if (p.hasPermission(perm) || p.hasPermission("*")) {
                int cd = sec.getInt(key, bestCooldown);
                if (cd < bestCooldown) bestCooldown = cd; // prend le cooldown le plus avantageux
            }
        }

        return bestCooldown;
    }

    /**
     * Applique un cooldown au joueur
     */
    public void applyCooldown(Player p, String base, int seconds) {
        cooldowns.putIfAbsent(p.getUniqueId(), new HashMap<>());
        long until = (System.currentTimeMillis() / 1000L) + seconds;
        cooldowns.get(p.getUniqueId()).put(base, until);
    }

    /**
     * Retourne le temps restant en secondes
     */
    public long getRemaining(Player p, String base) {
        Map<String, Long> map = cooldowns.get(p.getUniqueId());
        if (map == null) return 0;
        Long until = map.get(base);
        if (until == null) return 0;
        return Math.max(until - (System.currentTimeMillis() / 1000L), 0);
    }

    public boolean isOnCooldown(Player p, String base) {
        return getRemaining(p, base) > 0;
    }

    public void clearCooldown(Player p, String base) {
        Map<String, Long> map = cooldowns.get(p.getUniqueId());
        if (map != null) map.remove(base);
    }
}
