package me.funtage.morecmd;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.funtage.morecmd.commands.*;
import me.funtage.morecmd.listeners.*;
import me.funtage.morecmd.managers.ChunkManager;
import me.funtage.morecmd.managers.CooldownManager;
import me.funtage.morecmd.managers.ZonePurgeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class MoreCmd extends JavaPlugin {
    private static MoreCmd instance;
    private ChunkManager chunkManager;
    private ZonePurgeManager purgeManager;
    private CooldownManager cooldownManager;
    private FileConfiguration messagesConfig;
    private FileConfiguration recipesConfig;
    private FileConfiguration xpRecipesConfig;
    private File xpRecipesFile;

    private final Set<UUID> bucheronPlayers = new HashSet<>();
    private final Set<UUID> minerPlayers = new HashSet<>();

    public static MoreCmd getInstance() { return instance; }
    public ChunkManager getChunkManager() { return chunkManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public Set<UUID> getBucheronPlayers() { return bucheronPlayers; }
    public Set<UUID> getMinerPlayers() { return minerPlayers; }
    public FileConfiguration getMessagesConfig() { return messagesConfig; }
    public FileConfiguration getRecipesConfig() { return recipesConfig; }
    public FileConfiguration getXpRecipesConfig() { return xpRecipesConfig; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadMessages();
        loadRecipes();
        loadXpRecipes();

        chunkManager = new ChunkManager();
        cooldownManager = new CooldownManager(this);
        initializeChunkCounts();

        getServer().getPluginManager().registerEvents(new LimitListener(chunkManager), this);

        // Commandes existantes
        getCommand("morecmd").setExecutor(new ReloadCommand());
        getCommand("furnace").setExecutor(new FurnaceCommand());
        getCommand("furnaceall").setExecutor(new FurnaceAllCommand());
        getCommand("bucheron").setExecutor(new BucheronCommand(this));

        FarmerCommand farmerCommand = new FarmerCommand(this);
        getCommand("farmer").setExecutor(farmerCommand);
        getServer().getPluginManager().registerEvents(new FarmerListener(farmerCommand, this), this);

        // Enregistrement PURGE
        purgeManager = new ZonePurgeManager(this, chunkManager);
        purgeManager.startPurgeTask();

        // Bucheron Listener
        getServer().getPluginManager().registerEvents(new BucheronListener(this), this);

        // 🎯 MINER : Command + Listener
        getCommand("miner").setExecutor(new MinerCommand(this));
        getServer().getPluginManager().registerEvents(new MinerListener(this), this);

        getLogger().info("MoreCmd has been enabled!");
    }

    @Override
    public void onDisable() {
        if (purgeManager != null) purgeManager.cancelPurgeTask();
        getLogger().info("MoreCmd has been disabled!");
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', this.messagesConfig.getString(path, path));
    }

    // -------------------------- LOADERS --------------------------
    public void loadMessages() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) saveResource("messages.yml", false);
        messagesConfig = YamlConfiguration.loadConfiguration(msgFile);
    }

    public void loadRecipes() {
        File recFile = new File(getDataFolder(), "recipes.yml");
        if (!recFile.exists()) saveResource("recipes.yml", false);
        recipesConfig = YamlConfiguration.loadConfiguration(recFile);
    }

    public void loadXpRecipes() {
        xpRecipesFile = new File(getDataFolder(), "xp_recipe.yml");

        if (!xpRecipesFile.exists()) {
            try {
                saveResource("xp_recipe.yml", false);
            } catch (IllegalArgumentException e) {
                try {
                    xpRecipesFile.createNewFile();
                } catch (IOException io) { io.printStackTrace(); }
            }
        }
        xpRecipesConfig = YamlConfiguration.loadConfiguration(xpRecipesFile);
    }

    private void initializeChunkCounts() {
        Bukkit.getWorlds().forEach(world -> {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity e : chunk.getEntities()) {
                    if (e instanceof LivingEntity) {
                        chunkManager.addEntity(chunk, e.getType());
                    }
                }
            }
        });
    }

    public void reloadPlugin() {
        reloadConfig();
        loadMessages();
        loadRecipes();
        loadXpRecipes();
        if (purgeManager != null) purgeManager.cancelPurgeTask();
        purgeManager = new ZonePurgeManager(this, chunkManager);
        purgeManager.startPurgeTask();
    }
}