// src/main/java/de/novateams/managers/TeamManager.java
package de.novateams.managers;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import de.novateams.models.TeamRole;
import de.novateams.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamManager {
    
    private final NovaTeams plugin;
    
    public TeamManager(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    public Team createTeam(String name, Player owner) {
        if (plugin.getTeamCache().existsByName(name)) {
            return null;
        }
        
        UUID teamId = UUID.randomUUID();
        Team team = new Team(teamId, name, owner.getUniqueId());
        plugin.getTeamCache().put(team);
        
        return team;
    }
    
    public boolean deleteTeam(Team team) {
        // Claims entfernen
        plugin.getClaimCache().removeTeamClaims(team.getId());
        
        // Team aus Cache entfernen
        plugin.getTeamCache().remove(team.getId());
        
        return true;
    }
    
    public void addXp(Team team, long amount, String source) {
        if (amount <= 0) return;
        
        int oldLevel = team.getLevel();
        team.addXp(amount);
        
        // Level prüfen
        int newLevel = plugin.getConfigManager().getLevelForXp(team.getXp());
        if (newLevel > oldLevel && newLevel <= plugin.getConfigManager().getMaxLevel()) {
            team.setLevel(newLevel);
            onLevelUp(team, oldLevel, newLevel);
        }
        
        // Benachrichtigung an Online-Mitglieder
        team.getMembers().keySet().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                MessageUtil.send(player, "xp-gained", "xp", amount, "source", source);
            }
        });
    }
    
    private void onLevelUp(Team team, int oldLevel, int newLevel) {
        // Backpack upgraden
        int oldSize = plugin.getConfigManager().getBackpackSize(oldLevel);
        int newSize = plugin.getConfigManager().getBackpackSize(newLevel);
        
        if (newSize > oldSize) {
            team.getBackpack().setSize(newSize);
            
            team.getMembers().keySet().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    MessageUtil.send(player, "backpack-upgraded", "slots", newSize);
                }
            });
        }
        
        // Level-Up Nachricht
        team.getMembers().keySet().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                MessageUtil.send(player, "level-up", "level", newLevel);
            }
        });
    }
    
    public void processPlaytimeXP() {
        int xpPerHour = plugin.getConfigManager().getConfig().getInt("xp.playtime-per-hour", 5);
        if (xpPerHour <= 0) return;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
            if (team == null) continue;
            
            // 5 Minuten tracken
            team.addPlayTime(player.getUniqueId(), 5);
            
            // Alle 60 Minuten XP vergeben
            long totalMinutes = team.getPlayTime(player.getUniqueId());
            if (totalMinutes % 60 == 0 && totalMinutes > 0) {
                addXp(team, xpPerHour, "Spielzeit");
            }
        }
    }
    
    public boolean canManageRole(TeamRole managerRole, TeamRole targetRole) {
        return managerRole.getLevel() > targetRole.getLevel();
    }
    
    public boolean canPromote(TeamRole managerRole, TeamRole targetRole) {
        // Kann nur zu einer Rolle unter der eigenen befördern
        TeamRole nextRole = targetRole.getNextHigher();
        return managerRole.getLevel() > nextRole.getLevel();
    }
}
