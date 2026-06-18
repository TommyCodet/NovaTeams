// src/main/java/de/novateams/data/DataManager.java
package de.novateams.data;

import de.novateams.NovaTeams;
import de.novateams.models.*;
import de.novateams.utils.ChunkKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class DataManager {
    
    private final NovaTeams plugin;
    private final File teamsFile;
    private final File claimsFile;
    
    public DataManager(NovaTeams plugin) {
        this.plugin = plugin;
        this.teamsFile = new File(plugin.getDataFolder(), "teams.yml");
        this.claimsFile = new File(plugin.getDataFolder(), "claims.yml");
    }
    
    public void loadAllData() {
        loadTeams();
        loadClaims();
    }
    
    public void saveAllData() {
        saveTeams();
        saveClaims();
    }
    
    private void loadTeams() {
        if (!teamsFile.exists()) return;
        
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(teamsFile);
        ConfigurationSection teamsSection = yaml.getConfigurationSection("teams");
        if (teamsSection == null) return;
        
        for (String idStr : teamsSection.getKeys(false)) {
            try {
                ConfigurationSection ts = teamsSection.getConfigurationSection(idStr);
                if (ts == null) continue;
                
                UUID id = UUID.fromString(idStr);
                String name = ts.getString("name");
                UUID owner = UUID.fromString(ts.getString("owner"));
                
                Team team = new Team(id, name, owner);
                team.setLevel(ts.getInt("level", 1));
                team.setXp(ts.getLong("xp", 0));
                team.setCreatedAt(ts.getLong("created-at", System.currentTimeMillis()));
                
                // Members
                ConfigurationSection members = ts.getConfigurationSection("members");
                if (members != null) {
                    for (String memberStr : members.getKeys(false)) {
                        UUID memberId = UUID.fromString(memberStr);
                        TeamRole role = TeamRole.valueOf(members.getString(memberStr));
                        team.addMember(memberId, role);
                    }
                }
                
                // Biomes
                List<String> biomes = ts.getStringList("discovered-biomes");
                team.setDiscoveredBiomes(new HashSet<>(biomes));
                
                // Playtime
                ConfigurationSection playTime = ts.getConfigurationSection("playtime");
                if (playTime != null) {
                    Map<UUID, Long> times = new HashMap<>();
                    for (String playerStr : playTime.getKeys(false)) {
                        times.put(UUID.fromString(playerStr), playTime.getLong(playerStr));
                    }
                    team.setPlayTime(times);
                }
                
                // Backpack
                int bpSize = ts.getInt("backpack.size", 9);
                TeamBackpack backpack = new TeamBackpack(bpSize);
                ConfigurationSection bpContents = ts.getConfigurationSection("backpack.contents");
                if (bpContents != null) {
                    Map<Integer, ItemStack> contents = new HashMap<>();
                    for (String slotStr : bpContents.getKeys(false)) {
                        int slot = Integer.parseInt(slotStr);
                        ItemStack item = bpContents.getItemStack(slotStr);
                        if (item != null) {
                            contents.put(slot, item);
                        }
                    }
                    backpack.setContents(contents);
                }
                team.setBackpack(backpack);
                
                // Pending Invites
                List<String> invites = ts.getStringList("pending-invites");
                invites.forEach(inv -> team.addInvite(UUID.fromString(inv)));
                
                plugin.getTeamCache().put(team);
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Fehler beim Laden von Team " + idStr, e);
            }
        }
        
        plugin.getLogger().info("Loaded " + plugin.getTeamCache().getAllTeams().size() + " teams.");
    }
    
    private void saveTeams() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection teamsSection = yaml.createSection("teams");
        
        for (Team team : plugin.getTeamCache().getAllTeams()) {
            ConfigurationSection ts = teamsSection.createSection(team.getId().toString());
            
            ts.set("name", team.getName());
            ts.set("owner", team.getOwner().toString());
            ts.set("level", team.getLevel());
            ts.set("xp", team.getXp());
            ts.set("created-at", team.getCreatedAt());
            
            // Members
            ConfigurationSection members = ts.createSection("members");
            team.getMembers().forEach((uuid, role) -> 
                members.set(uuid.toString(), role.name()));
            
            // Biomes
            ts.set("discovered-biomes", new ArrayList<>(team.getDiscoveredBiomes()));
            
            // Playtime
            ConfigurationSection playTime = ts.createSection("playtime");
            team.getPlayTime().forEach((uuid, time) -> 
                playTime.set(uuid.toString(), time));
            
            // Backpack
            ts.set("backpack.size", team.getBackpack().getSize());
            ConfigurationSection bpContents = ts.createSection("backpack.contents");
            team.getBackpack().getContents().forEach((slot, item) -> 
                bpContents.set(String.valueOf(slot), item));
            
            // Pending Invites
            ts.set("pending-invites", team.getPendingInvites().stream()
                .map(UUID::toString).toList());
        }
        
        try {
            yaml.save(teamsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Speichern der Teams", e);
        }
    }
    
    private void loadClaims() {
        if (!claimsFile.exists()) return;
        
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(claimsFile);
        ConfigurationSection claimsSection = yaml.getConfigurationSection("claims");
        if (claimsSection == null) return;
        
        for (String keyStr : claimsSection.getKeys(false)) {
            try {
                ConfigurationSection cs = claimsSection.getConfigurationSection(keyStr);
                if (cs == null) continue;
                
                ChunkKey key = ChunkKey.deserialize(keyStr);
                UUID teamId = UUID.fromString(cs.getString("team"));
                long claimedAt = cs.getLong("claimed-at");
                UUID claimedBy = UUID.fromString(cs.getString("claimed-by"));
                
                Claim claim = new Claim(key, teamId, claimedAt, claimedBy);
                plugin.getClaimCache().put(claim);
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Fehler beim Laden von Claim " + keyStr, e);
            }
        }
        
        plugin.getLogger().info("Loaded " + plugin.getClaimCache().getAllClaims().size() + " claims.");
    }
    
    private void saveClaims() {
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection claimsSection = yaml.createSection("claims");
        
        for (Claim claim : plugin.getClaimCache().getAllClaims()) {
            ConfigurationSection cs = claimsSection.createSection(claim.chunkKey().serialize());
            cs.set("team", claim.teamId().toString());
            cs.set("claimed-at", claim.claimedAt());
            cs.set("claimed-by", claim.claimedBy().toString());
        }
        
        try {
            yaml.save(claimsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Speichern der Claims", e);
        }
    }
}
