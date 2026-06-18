// src/main/java/de/novateams/NovaTeams.java
package de.novateams;

import de.novateams.api.NovaTeamsAPI;
import de.novateams.cache.ClaimCache;
import de.novateams.cache.TeamCache;
import de.novateams.commands.TeamCommand;
import de.novateams.commands.TeamTabCompleter;
import de.novateams.config.ConfigManager;
import de.novateams.data.DataManager;
import de.novateams.hooks.AuraSkillsHook;
import de.novateams.hooks.EliteMobsHook;
import de.novateams.listeners.*;
import de.novateams.managers.BackpackManager;
import de.novateams.managers.ClaimManager;
import de.novateams.managers.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NovaTeams extends JavaPlugin {
    
    private static NovaTeams instance;
    
    private ConfigManager configManager;
    private DataManager dataManager;
    private TeamManager teamManager;
    private ClaimManager claimManager;
    private BackpackManager backpackManager;
    private TeamCache teamCache;
    private ClaimCache claimCache;
    
    private AuraSkillsHook auraSkillsHook;
    private EliteMobsHook eliteMobsHook;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Konfig initialisieren
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Caches initialisieren
        teamCache = new TeamCache();
        claimCache = new ClaimCache();
        
        // Manager initialisieren
        dataManager = new DataManager(this);
        teamManager = new TeamManager(this);
        claimManager = new ClaimManager(this);
        backpackManager = new BackpackManager(this);
        
        // Daten laden
        dataManager.loadAllData();
        
        // Hooks für optionale Plugins
        setupHooks();
        
        // Commands registrieren
        registerCommands();
        
        // Listener registrieren
        registerListeners();
        
        // Scheduled Tasks
        startScheduledTasks();
        
        getLogger().info("NovaTeams wurde erfolgreich aktiviert!");
    }
    
    @Override
    public void onDisable() {
        // Daten speichern
        if (dataManager != null) {
            dataManager.saveAllData();
        }
        
        getLogger().info("NovaTeams wurde deaktiviert.");
    }
    
    private void setupHooks() {
        // AuraSkills Hook
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") != null) {
            auraSkillsHook = new AuraSkillsHook(this);
            auraSkillsHook.register();
            getLogger().info("AuraSkills Integration aktiviert!");
        }
        
        // EliteMobs Hook
        if (Bukkit.getPluginManager().getPlugin("EliteMobs") != null) {
            eliteMobsHook = new EliteMobsHook(this);
            eliteMobsHook.register();
            getLogger().info("EliteMobs Integration aktiviert!");
        }
    }
    
    private void registerCommands() {
        var teamCommand = getCommand("team");
        if (teamCommand != null) {
            teamCommand.setExecutor(new TeamCommand(this));
            teamCommand.setTabCompleter(new TeamTabCompleter(this));
        }
    }
    
    private void registerListeners() {
        var pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new XPListener(this), this);
        pm.registerEvents(new ClaimProtectionListener(this), this);
        pm.registerEvents(new GUIListener(this), this);
    }
    
    private void startScheduledTasks() {
        int saveInterval = configManager.getConfig().getInt("performance.async-save-interval", 60) * 20;
        int cacheInterval = configManager.getConfig().getInt("performance.cache-cleanup-interval", 300) * 20;
        
        // Auto-Save
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            dataManager.saveAllData();
        }, saveInterval, saveInterval);
        
        // Cache-Cleanup
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            teamCache.cleanup();
            claimCache.cleanup();
        }, cacheInterval, cacheInterval);
        
        // Spielzeit-XP (alle 5 Minuten prüfen)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            teamManager.processPlaytimeXP();
        }, 6000L, 6000L);
    }
    
    // Getter
    public static NovaTeams getInstance() { return instance; }
    public static NovaTeamsAPI getAPI() { return new NovaTeamsAPI(instance); }
    
    public ConfigManager getConfigManager() { return configManager; }
    public DataManager getDataManager() { return dataManager; }
    public TeamManager getTeamManager() { return teamManager; }
    public ClaimManager getClaimManager() { return claimManager; }
    public BackpackManager getBackpackManager() { return backpackManager; }
    public TeamCache getTeamCache() { return teamCache; }
    public ClaimCache getClaimCache() { return claimCache; }
    public AuraSkillsHook getAuraSkillsHook() { return auraSkillsHook; }
    public EliteMobsHook getEliteMobsHook() { return eliteMobsHook; }
}
