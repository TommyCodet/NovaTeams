// src/main/java/de/novateams/api/NovaTeamsAPI.java
package de.novateams.api;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import de.novateams.utils.ChunkKey;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Öffentliche API für NovaTeams
 */
public class NovaTeamsAPI {
    
    private final NovaTeams plugin;
    
    public NovaTeamsAPI(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Holt das Team eines Spielers
     */
    public Optional<Team> getPlayerTeam(UUID playerId) {
        return Optional.ofNullable(plugin.getTeamCache().getByPlayer(playerId));
    }
    
    /**
     * Holt das Team eines Spielers
     */
    public Optional<Team> getPlayerTeam(Player player) {
        return getPlayerTeam(player.getUniqueId());
    }
    
    /**
     * Holt ein Team nach Namen
     */
    public Optional<Team> getTeamByName(String name) {
        return Optional.ofNullable(plugin.getTeamCache().getByName(name));
    }
    
    /**
     * Holt ein Team nach ID
     */
    public Optional<Team> getTeamById(UUID teamId) {
        return Optional.ofNullable(plugin.getTeamCache().getById(teamId));
    }
    
    /**
     * Alle Teams
     */
    public Collection<Team> getAllTeams() {
        return plugin.getTeamCache().getAllTeams();
    }
    
    /**
     * Fügt XP zu einem Team hinzu
     */
    public void addTeamXp(Team team, long amount, String source) {
        plugin.getTeamManager().addXp(team, amount, source);
    }
    
    /**
     * Prüft, ob ein Chunk geclaimed ist
     */
    public boolean isChunkClaimed(Chunk chunk) {
        return plugin.getClaimCache().isClaimed(ChunkKey.from(chunk));
    }
    
    /**
     * Holt das Team eines geclaimten Chunks
     */
    public Optional<UUID> getClaimOwner(Chunk chunk) {
        return Optional.ofNullable(plugin.getClaimCache().getTeamId(ChunkKey.from(chunk)));
    }
    
    /**
     * Prüft, ob ein Spieler in einem Chunk interagieren darf
     */
    public boolean canPlayerInteract(Player player, Chunk chunk) {
        return plugin.getClaimManager().canInteract(player, chunk);
    }
}
