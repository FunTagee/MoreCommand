package me.funtage.morecmd.managers;

import java.util.HashMap;
import java.util.Map;
import me.funtage.morecmd.MoreCmd;
import org.bukkit.Material;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

public class XpManager {

    private static final MoreCmd plugin = MoreCmd.getInstance();
    private static final Map<Player, Double> xpFraction = new HashMap<>();

    public static boolean isEnabled() {
        return plugin.getConfig().getBoolean("xp-drop.enabled", false);
    }

    public static String getMode() {
        return plugin.getConfig().getString("xp-drop.mode", "per-item").toLowerCase();
    }

    public static String getDelivery() {
        return plugin.getConfig().getString("xp-drop.xp-delivery", "drop").toLowerCase();
    }

    public static double getPerItemValue() {
        return plugin.getXpRecipesConfig().getDouble("per-item", 1.0);
    }

    public static double getPerResult(Material mat) {
        return plugin.getXpRecipesConfig().getDouble("per-result." + mat.name(), 0.0);
    }

    public static double calculateXpForResult(Material result, int amount) {
        if (!isEnabled() || amount <= 0) return 0;

        String mode = getMode();
        if (mode.equals("per-item")) {
            return getPerItemValue() * amount;
        } else if (mode.equals("per-result")) {
            return getPerResult(result) * amount;
        }
        return 0;
    }

    public static void giveXp(Player p, double value) {
        if (value <= 0) return;

        double total = value + xpFraction.getOrDefault(p, 0.0);
        int whole = (int) total;
        double remainder = total - whole;

        if (whole > 0) {
            if (getDelivery().equals("add")) {
                p.giveExp(whole);
            } else {
                ExperienceOrb orb = p.getWorld().spawn(p.getLocation(), ExperienceOrb.class);
                orb.setExperience(whole);
            }
        }

        xpFraction.put(p, remainder);
    }

    public static void calculateAndGiveForResult(Player p, Material result, int amount) {
        giveXp(p, calculateXpForResult(result, amount));
    }

    public static void giveTotalXp(Player p, Map<Material, Integer> map) {
        double total = 0;
        for (Map.Entry<Material, Integer> e : map.entrySet()) {
            total += calculateXpForResult(e.getKey(), e.getValue());
        }
        giveXp(p, total);
    }
}
