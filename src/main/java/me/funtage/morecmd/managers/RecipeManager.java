package me.funtage.morecmd.managers;

import me.funtage.morecmd.MoreCmd;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class RecipeManager {

    private final MoreCmd plugin;
    private final Map<Material, Material> recipes = new HashMap<>();

    public RecipeManager(MoreCmd plugin) {
        this.plugin = plugin;
        loadRecipes();
    }

    public void loadRecipes() {
        recipes.clear();
        plugin.loadRecipes();
        plugin.getRecipesConfig().getConfigurationSection("recipes").getKeys(false)
                .forEach(key -> {
                    Material input = Material.getMaterial(key.toUpperCase());
                    Material output = Material.getMaterial(plugin.getRecipesConfig().getString("recipes." + key).toUpperCase());
                    if (input != null && output != null) {
                        recipes.put(input, output);
                    }
                });
    }

    public Material getResult(Material input) {
        return recipes.get(input);
    }

    public boolean canSmelt(Material input) {
        return recipes.containsKey(input);
    }
}
