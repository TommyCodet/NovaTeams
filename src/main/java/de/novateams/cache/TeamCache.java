// src/main/java/de/novateams/cache/TeamCache.java
package de.novateams.cache;

import de.novateams.models.Team;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamCache {
    
    private final Map<UUID, Team> teamsById = new ConcurrentHashMap<>();
    private final Map<String, UUID> teamsByName = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerTeams = new ConcurrentHashMap<>();
    
    public void put(Team team) {
        teamsById.put(team.getId(), team);
        teamsByName.put(team.getName().toLowerCase(), team.getId());
        team.getMembers().keySet().forEach(uuid -> playerTeams.put(uuid, team.getId()));
    }
    
    public void remove(UUID teamId) {
        Team team = teamsById.remove(teamId);
        if (team != null) {
            teamsByName.remove(team.getName().toLowerCase());
            team.getMembers().keySet().forEach(playerTeams::remove);
        }
    }
    
    public Team getById(UUID id) {
        return teamsById.get(id);
    }
    
    public Team getByName(String name) {
        UUID id = teamsByName.get(name.toLowerCase());
        return id != null ? teamsById.get(id) : null;
    }
    
    public Team getByPlayer(UUID playerId) {
        UUID teamId = playerTeams.get(playerId);
        return teamId != null ? teamsById.get(teamId) : null;
    }
    
    public void updatePlayerTeam(UUID playerId, UUID teamId) {
        if (teamId == null) {
            playerTeams.remove(playerId);
        } else {
            playerTeams.put(playerId, teamId);
        }
    }
    
    public Collection<Team> getAllTeams() {
        return Collections.unmodifiableCollection(teamsById.values());
    }
    
    public boolean existsByName(String name) {
        return teamsByName.containsKey(name.toLowerCase());
    }
    
    public void cleanup() {
        // Entferne verwaiste Player-Mappings
        playerTeams.entrySet().removeIf(entry -> {
            Team team = teamsById.get(entry.getValue());
            return team == null || !team.isMember(entry.getKey());
        });
    }
    
    public void clear() {
        teamsById.clear();
        teamsByName.clear();
        playerTeams.clear();
    }
}
