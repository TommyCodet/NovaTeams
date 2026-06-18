// src/main/java/de/novateams/listeners/PlayerListener.java
package de.novateams.listeners;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    private final NovaTeams plugin;
    
    public PlayerListener(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Cache aufwärmen bei Bedarf
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getBackpackManager().closeBackpack(event.getPlayer());
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // Nur bei Chunk-Wechsel prüfen
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }
        
        // Biom-Entdeckung
        Team team = plugin.getTeamCache().getByPlayer(event.getPlayer().getUniqueId());
        if (team == null) return;
        
        String biome = event.getTo().getBlock().getBiome().name();
        if (team.discoverBiome(biome)) {
            int xp = plugin.getConfigManager().getConfig().getInt("xp.biome-discovery", 10);
            plugin.getTeamManager().addXp(team, xp, "Biom: " + biome);
        }
    }
}
