// src/main/java/de/novateams/managers/BackpackManager.java
package de.novateams.managers;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import de.novateams.models.TeamBackpack;
import de.novateams.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackpackManager {
    
    private final NovaTeams plugin;
    private final Map<UUID, UUID> openBackpacks = new HashMap<>(); // Player -> Team
    
    public BackpackManager(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    public void openBackpack(Player player, Team team) {
        TeamBackpack backpack = team.getBackpack();
        
        String title = plugin.getConfigManager().getConfig()
            .getString("gui.backpack.title", "&8Team-Backpack &7(Level %level%)")
            .replace("%level%", String.valueOf(team.getLevel()));
        
        Inventory inv = Bukkit.createInventory(null, backpack.getSize(), 
            MessageUtil.parse(title));
        
        // Items laden
        backpack.getContents().forEach((slot, item) -> {
            if (slot < inv.getSize()) {
                inv.setItem(slot, item);
            }
        });
        
        openBackpacks.put(player.getUniqueId(), team.getId());
        player.openInventory(inv);
    }
    
    public void saveBackpack(Player player, Inventory inventory) {
        UUID teamId = openBackpacks.remove(player.getUniqueId());
        if (teamId == null) return;
        
        Team team = plugin.getTeamCache().getById(teamId);
        if (team == null) return;
        
        Map<Integer, ItemStack> contents = new HashMap<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                contents.put(i, item);
            }
        }
        
        team.getBackpack().setContents(contents);
    }
    
    public boolean isBackpackOpen(Player player) {
        return openBackpacks.containsKey(player.getUniqueId());
    }
    
    public void closeBackpack(Player player) {
        openBackpacks.remove(player.getUniqueId());
    }
}
