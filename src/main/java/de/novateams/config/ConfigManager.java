// src/main/java/de/novateams/config/ConfigManager.java
package de.novateams.config;

import de.novateams.NovaTeams;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ConfigManager {
    
    private final NovaTeams plugin;
    private FileConfiguration config;
    
    // Cached values für Performance
    private final Map<String, Integer> mobXpCache = new HashMap<>();
    private final Map<String, Integer> oreXpCache = new HashMap<>();
    private final Map<String, Integer> advancementXpCache = new HashMap<>();
    private final NavigableMap<Integer, Integer> backpackSizes = new TreeMap<>();
    private final NavigableMap<Integer, Integer> maxClaims = new TreeMap<>();
    private final NavigableMap<Integer, Long> levelRequirements = new TreeMap<>();
    
    public ConfigManager(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        cacheValues();
    }
    
    private void cacheValues() {
        // Mob XP
        mobXpCache.clear();
        var mobSection = config.getConfigurationSection("xp.mobs");
        if (mobSection != null) {
            mobSection.getKeys(false).forEach(key -> 
                mobXpCache.put(key, mobSection.getInt(key)));
        }
        
        // Ore XP
        oreXpCache.clear();
        var oreSection = config.getConfigurationSection("xp.ores");
        if (oreSection != null) {
            oreSection.getKeys(false).forEach(key -> 
                oreXpCache.put(key, oreSection.getInt(key)));
        }
        
        // Advancement XP
        advancementXpCache.clear();
        var advSection = config.getConfigurationSection("xp.advancements.special");
        if (advSection != null) {
            advSection.getKeys(false).forEach(key -> 
                advancementXpCache.put(key, advSection.getInt(key)));
        }
        
        // Backpack Sizes
        backpackSizes.clear();
        var bpSection = config.getConfigurationSection("backpack.sizes");
        if (bpSection != null) {
            bpSection.getKeys(false).forEach(key -> 
                backpackSizes.put(Integer.parseInt(key), bpSection.getInt(key)));
        }
        
        // Max Claims
        maxClaims.clear();
        var claimSection = config.getConfigurationSection("claims.max-per-level");
        if (claimSection != null) {
            claimSection.getKeys(false).forEach(key -> 
                maxClaims.put(Integer.parseInt(key), claimSection.getInt(key)));
        }
        
        // Level Requirements
        levelRequirements.clear();
        var levelSection = config.getConfigurationSection("levels.requirements");
        if (levelSection != null) {
            levelSection.getKeys(false).forEach(key -> 
                levelRequirements.put(Integer.parseInt(key), levelSection.getLong(key)));
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public int getMobXp(String mobType) {
        return mobXpCache.getOrDefault(mobType, 0);
    }
    
    public int getOreXp(String oreType) {
        return oreXpCache.getOrDefault(oreType, 0);
    }
    
    public int getAdvancementXp(String advancement) {
        return advancementXpCache.getOrDefault(advancement, 
            config.getInt("xp.advancements.default", 15));
    }
    
    public int getBackpackSize(int level) {
        var entry = backpackSizes.floorEntry(level);
        return entry != null ? entry.getValue() : 9;
    }
    
    public int getMaxClaims(int level) {
        var entry = maxClaims.floorEntry(level);
        return entry != null ? entry.getValue() : 5;
    }
    
    public long getXpForLevel(int level) {
        return levelRequirements.getOrDefault(level, 0L);
    }
    
    public int getLevelForXp(long xp) {
        int level = 1;
        for (var entry : levelRequirements.entrySet()) {
            if (xp >= entry.getValue()) {
                level = entry.getKey();
            } else {
                break;
            }
        }
        return level;
    }
    
    public int getMaxLevel() {
        return levelRequirements.isEmpty() ? 20 : levelRequirements.lastKey();
    }
}
