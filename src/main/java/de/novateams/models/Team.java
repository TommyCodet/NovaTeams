// src/main/java/de/novateams/models/Team.java
package de.novateams.models;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Team {
    
    private final UUID id;
    private String name;
    private UUID owner;
    private int level;
    private long xp;
    private long createdAt;
    
    private final Map<UUID, TeamRole> members;
    private final Set<UUID> pendingInvites;
    private final Set<String> discoveredBiomes;
    private final Map<UUID, Long> playTime; // Spielzeit in Minuten
    
    private TeamBackpack backpack;
    
    public Team(UUID id, String name, UUID owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.level = 1;
        this.xp = 0;
        this.createdAt = System.currentTimeMillis();
        this.members = new ConcurrentHashMap<>();
        this.pendingInvites = ConcurrentHashMap.newKeySet();
        this.discoveredBiomes = ConcurrentHashMap.newKeySet();
        this.playTime = new ConcurrentHashMap<>();
        this.backpack = new TeamBackpack(9);
        
        members.put(owner, TeamRole.OWNER);
    }
    
    // ID & Name
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // Owner
    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) {
        if (this.owner != null) {
            members.put(this.owner, TeamRole.CO_OWNER);
        }
        this.owner = owner;
        members.put(owner, TeamRole.OWNER);
    }
    
    // Level & XP
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }
    
    public void addXp(long amount) {
        this.xp += amount;
    }
    
    // Timestamps
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    // Members
    public Map<UUID, TeamRole> getMembers() { return Collections.unmodifiableMap(members); }
    
    public void addMember(UUID uuid, TeamRole role) {
        members.put(uuid, role);
    }
    
    public void removeMember(UUID uuid) {
        members.remove(uuid);
        playTime.remove(uuid);
    }
    
    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }
    
    public TeamRole getRole(UUID uuid) {
        return members.get(uuid);
    }
    
    public void setRole(UUID uuid, TeamRole role) {
        if (members.containsKey(uuid)) {
            members.put(uuid, role);
        }
    }
    
    public int getMemberCount() {
        return members.size();
    }
    
    // Invites
    public Set<UUID> getPendingInvites() { return Collections.unmodifiableSet(pendingInvites); }
    
    public void addInvite(UUID uuid) {
        pendingInvites.add(uuid);
    }
    
    public void removeInvite(UUID uuid) {
        pendingInvites.remove(uuid);
    }
    
    public boolean hasInvite(UUID uuid) {
        return pendingInvites.contains(uuid);
    }
    
    // Biomes
    public Set<String> getDiscoveredBiomes() { return Collections.unmodifiableSet(discoveredBiomes); }
    
    public boolean discoverBiome(String biome) {
        return discoveredBiomes.add(biome);
    }
    
    public void setDiscoveredBiomes(Set<String> biomes) {
        discoveredBiomes.clear();
        discoveredBiomes.addAll(biomes);
    }
    
    // Playtime
    public Map<UUID, Long> getPlayTime() { return Collections.unmodifiableMap(playTime); }
    
    public void addPlayTime(UUID uuid, long minutes) {
        playTime.merge(uuid, minutes, Long::sum);
    }
    
    public long getPlayTime(UUID uuid) {
        return playTime.getOrDefault(uuid, 0L);
    }
    
    public void setPlayTime(Map<UUID, Long> times) {
        playTime.clear();
        playTime.putAll(times);
    }
    
    // Backpack
    public TeamBackpack getBackpack() { return backpack; }
    public void setBackpack(TeamBackpack backpack) { this.backpack = backpack; }
}
