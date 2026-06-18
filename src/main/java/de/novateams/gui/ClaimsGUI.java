// src/main/java/de/novateams/gui/ClaimsGUI.java
package de.novateams.gui;

import de.novateams.NovaTeams;
import de.novateams.models.Claim;
import de.novateams.models.Team;
import de.novateams.utils.ItemBuilder;
import de.novateams.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class ClaimsGUI {
    
    private final NovaTeams plugin;
    private final Player player;
    private final Team team;
    
    public ClaimsGUI(NovaTeams plugin, Player player, Team team) {
        this.plugin = plugin;
        this.player = player;
        this.team = team;
    }
    
    public void open() {
        String title = plugin.getConfigManager().getConfig()
            .getString("gui.claims.title", "&8Claims - &6%team%")
            .replace("%team%", team.getName());
        
        Set<Claim> claims = plugin.getClaimCache().getTeamClaims(team.getId());
        int size = Math.min(54, ((claims.size() / 9) + 2) * 9);
        size = Math.max(27, size);
        
        Inventory inv = Bukkit.createInventory(null, size, MessageUtil.parse(title));
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        
        int slot = 0;
        for (Claim claim : claims) {
            if (slot >= size - 9) break;
            
            String world = claim.chunkKey().world();
            int x = claim.chunkKey().x() * 16;
            int z = claim.chunkKey().z() * 16;
            String date = sdf.format(new Date(claim.claimedAt()));
            String claimedBy = Bukkit.getOfflinePlayer(claim.claimedBy()).getName();
            
            inv.setItem(slot++, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&eChunk &7[" + claim.chunkKey().x() + ", " + claim.chunkKey().z() + "]")
                .lore("&7Welt: &e" + world,
                      "&7Position: &e" + x + ", " + z,
                      "&7Geclaimed: &e" + date,
                      "&7Von: &e" + claimedBy)
                .build());
        }
        
        // Info
        int maxClaims = plugin.getConfigManager().getMaxClaims(team.getLevel());
        inv.setItem(size - 9, new ItemBuilder(Material.PAPER)
            .name("&6Claim-Info")
            .lore("&7Genutzt: &e" + claims.size() + "&7/&e" + maxClaims,
                  "&7Level: &e" + team.getLevel(),
                  "",
                  "&7Nutze &e/team claim &7zum Claimen",
                  "&7Nutze &e/team map &7für eine Übersicht")
            .build());
        
        // Back Button
        inv.setItem(size - 5, new ItemBuilder(Material.ARROW)
            .name("&cZurück")
            .build());
        
        player.openInventory(inv);
    }
}
