// src/main/java/de/novateams/utils/ChunkKey.java
package de.novateams.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public record ChunkKey(String world, int x, int z) {
    
    public static ChunkKey from(Chunk chunk) {
        return new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }
    
    public static ChunkKey from(Location location) {
        return new ChunkKey(
            location.getWorld().getName(),
            location.getBlockX() >> 4,
            location.getBlockZ() >> 4
        );
    }
    
    public String serialize() {
        return world + ";" + x + ";" + z;
    }
    
    public static ChunkKey deserialize(String str) {
        String[] parts = str.split(";");
        return new ChunkKey(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}
