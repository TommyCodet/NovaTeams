// src/main/java/de/novateams/cache/ClaimCache.java
package de.novateams.cache;

import de.novateams.models.Claim;
import de.novateams.utils.ChunkKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClaimCache {
    
    private final Map<ChunkKey, Claim> claims = new ConcurrentHashMap<>();
    private final Map<UUID, Set<ChunkKey>> teamClaims = new ConcurrentHashMap<>();
    
    public void put(Claim claim) {
        claims.put(claim.chunkKey(), claim);
        teamClaims.computeIfAbsent(claim.teamId(), k -> ConcurrentHashMap.newKeySet())
                  .add(claim.chunkKey());
    }
    
    public void remove(ChunkKey key) {
        Claim claim = claims.remove(key);
        if (claim != null) {
            Set<ChunkKey> teamChunks = teamClaims.get(claim.teamId());
            if (teamChunks != null) {
                teamChunks.remove(key);
            }
        }
    }
    
    public Claim get(ChunkKey key) {
        return claims.get(key);
    }
    
    public boolean isClaimed(ChunkKey key) {
        return claims.containsKey(key);
    }
    
    public UUID getTeamId(ChunkKey key) {
        Claim claim = claims.get(key);
        return claim != null ? claim.teamId() : null;
    }
    
    public Set<Claim> getTeamClaims(UUID teamId) {
        Set<ChunkKey> keys = teamClaims.get(teamId);
        if (keys == null) return Collections.emptySet();
        return keys.stream()
                   .map(claims::get)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toUnmodifiableSet());
    }
    
    public int getTeamClaimCount(UUID teamId) {
        Set<ChunkKey> keys = teamClaims.get(teamId);
        return keys != null ? keys.size() : 0;
    }
    
    public Collection<Claim> getAllClaims() {
        return Collections.unmodifiableCollection(claims.values());
    }
    
    public void removeTeamClaims(UUID teamId) {
        Set<ChunkKey> keys = teamClaims.remove(teamId);
        if (keys != null) {
            keys.forEach(claims::remove);
        }
    }
    
    public void cleanup() {
        // Keine aktive Bereinigung nötig, da Claims persistent sind
    }
    
    public void clear() {
        claims.clear();
        teamClaims.clear();
    }
}
