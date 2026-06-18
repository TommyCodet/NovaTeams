// src/main/java/de/novateams/gui/MembersGUI.java
package de.novateams.gui;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import de.novateams.models.TeamRole;
import de.novateams.utils.ItemBuilder;
import de.novateams.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class MembersGUI {
    
    private final NovaTeams plugin;
    private final Player player;
    private final Team team;
    
    public MembersGUI(NovaTeams plugin, Player player, Team team) {
        this.plugin = plugin;
        this.player = player;
        this.team = team;
    }
    
    public void open() {
        String title = plugin.getConfigManager().getConfig()
            .getString("gui.members.title", "&8Mitglieder - &6%team%")
            .replace("%team%", team.getName());
        
        int size = Math.min(54, ((team.getMemberCount() / 9) + 2) * 9);
        Inventory inv = Bukkit.createInventory(null, size, MessageUtil.parse(title));
        
        int slot = 0;
        for (var entry : team.getMembers().entrySet()) {
            if (slot >= size - 9) break; // Platz für Back-Button lassen
            
            UUID uuid = entry.getKey();
            TeamRole role = entry.getValue();
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
            
            String status = member.isOnline() ? "&aOnline" : "&cOffline";
            
            inv.setItem(slot++, new ItemBuilder(Material.PLAYER_HEAD)
                .skullOwner(member)
                .name("&e" + member.getName())
                .lore("&7Rolle: &e" + role.getDisplayName(),
                      "&7Status: " + status)
                .build());
        }
        
        // Back Button
        inv.setItem(size - 5, new ItemBuilder(Material.ARROW)
            .name("&cZurück")
            .build());
        
        player.openInventory(inv);
    }
}
