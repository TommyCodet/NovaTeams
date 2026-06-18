// src/main/java/de/novateams/models/Claim.java
package de.novateams.models;

import de.novateams.utils.ChunkKey;
import java.util.UUID;

public record Claim(
    ChunkKey chunkKey,
    UUID teamId,
    long claimedAt,
    UUID claimedBy
) {
    public Claim(ChunkKey chunkKey, UUID teamId, UUID claimedBy) {
        this(chunkKey, teamId, System.currentTimeMillis(), claimedBy);
    }
}
