// src/main/java/de/novateams/listeners/XPListener.java
package de.novateams.listeners;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class XPListener implements Listener {
    
    private final NovaTeams plugin;
    
    public XPListener(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        
        Team team = plugin.getTeamCache().getByPlayer(killer.getUniqueId());
        if (team == null) return;
        
        EntityType type = event.getEntityType();
        
        // Spieler-Kill
        if (type == EntityType.PLAYER) {
            int xp = plugin.getConfigManager().getConfig().getInt("xp.player-kill", 50);
            plugin.getTeamManager().addXp(team, xp, "Spieler-Kill");
            return;
        }
        
        // Mob-Kill
        int xp = plugin.getConfigManager().getMobXp(type.name());
        if (xp > 0) {
            plugin.getTeamManager().addXp(team, xp, type.name());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) return;
        
        String blockType = event.getBlock().getType().name();
        int xp = plugin.getConfigManager().getOreXp(blockType);
        
        if (xp > 0) {
            plugin.getTeamManager().addXp(team, xp, blockType);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        // Rezept-Advancements ignorieren
        if (event.getAdvancement().getKey().getKey().startsWith("recipes/")) {
            return;
        }
        
        Player player = event.getPlayer();
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) return;
        
        String key = event.getAdvancement().getKey().toString();
        int xp = plugin.getConfigManager().getAdvancementXp(key);
        plugin.getTeamManager().addXp(team, xp, "Advancement");
    }
}
