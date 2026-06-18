// src/main/java/de/novateams/managers/ClaimManager.java
package de.novateams.managers;

import de.novateams.NovaTeams;
import de.novateams.models.Claim;
import de.novateams.models.Team;
import de.novateams.utils.ChunkKey;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClaimManager {
    
    private final NovaTeams plugin;
    
    public ClaimManager(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    public ClaimResult claim(Chunk chunk, Player player, Team team) {
        ChunkKey key = ChunkKey.from(chunk);
        
        // Bereits geclaimed?
        if (plugin.getClaimCache().isClaimed(key)) {
            UUID existingTeam = plugin.getClaimCache().getTeamId(key);
            if (existingTeam.equals(team.getId())) {
                return ClaimResult.ALREADY_OWNED;
            }
            return ClaimResult.ALREADY_CLAIMED;
        }
        
        // Max Claims erreicht?
        int maxClaims = plugin.getConfigManager().getMaxClaims(team.getLevel());
        int currentClaims = plugin.getClaimCache().getTeamClaimCount(team.getId());
        if (currentClaims >= maxClaims) {
            return ClaimResult.MAX_REACHED;
        }
        
        // Claim erstellen
        Claim claim = new Claim(key, team.getId(), player.getUniqueId());
        plugin.getClaimCache().put(claim);
        
        // XP vergeben
        int claimXp = plugin.getConfigManager().getConfig().getInt("xp.chunk-claim", 3);
        plugin.getTeamManager().addXp(team, claimXp, "Chunk-Claim");
        
        return ClaimResult.SUCCESS;
    }
    
    public boolean unclaim(Chunk chunk, Team team) {
        ChunkKey key = ChunkKey.from(chunk);
        
        Claim claim = plugin.getClaimCache().get(key);
        if (claim == null || !claim.teamId().equals(team.getId())) {
            return false;
        }
        
        plugin.getClaimCache().remove(key);
        return true;
    }
    
    public boolean canInteract(Player player, Chunk chunk) {
        // Bypass-Permission
        if (player.hasPermission("novateams.bypass.claims")) {
            return true;
        }
        
        ChunkKey key = ChunkKey.from(chunk);
        Claim claim = plugin.getClaimCache().get(key);
        
        // Nicht geclaimed = jeder kann interagieren
        if (claim == null) {
            return true;
        }
        
        // Ist der Spieler im Team?
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        return team != null && team.getId().equals(claim.teamId());
    }
    
    public enum ClaimResult {
        SUCCESS,
        ALREADY_CLAIMED,
        ALREADY_OWNED,
        MAX_REACHED
    }
}
