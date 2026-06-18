// src/main/java/de/novateams/gui/TeamMainGUI.java
package de.novateams.gui;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import de.novateams.utils.ItemBuilder;
import de.novateams.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class TeamMainGUI {
    
    private final NovaTeams plugin;
    private final Player player;
    private final Team team;
    
    public TeamMainGUI(NovaTeams plugin, Player player, Team team) {
        this.plugin = plugin;
        this.player = player;
        this.team = team;
    }
    
    public void open() {
        String title = plugin.getConfigManager().getConfig()
            .getString("gui.main-menu.title", "&8Team: &6%team%")
            .replace("%team%", team.getName());
        
        Inventory inv = Bukkit.createInventory(null, 27, MessageUtil.parse(title));
        
        // Mitglieder
        inv.setItem(11, new ItemBuilder(Material.PLAYER_HEAD)
            .name("&eMitglieder")
            .lore("&7Anzahl: &e" + team.getMemberCount(),
                  "",
                  "&aKlicken zum Anzeigen")
            .build());
        
        // Backpack
        int bpSize = team.getBackpack().getSize();
        inv.setItem(13, new ItemBuilder(Material.CHEST)
            .name("&eTeam-Backpack")
            .lore("&7Größe: &e" + bpSize + " Slots",
                  "&7Level: &e" + team.getLevel(),
                  "",
                  "&aKlicken zum Öffnen")
            .build());
        
        // Claims
        int maxClaims = plugin.getConfigManager().getMaxClaims(team.getLevel());
        int currentClaims = plugin.getClaimCache().getTeamClaimCount(team.getId());
        inv.setItem(15, new ItemBuilder(Material.GRASS_BLOCK)
            .name("&eClaims")
            .lore("&7Genutzt: &e" + currentClaims + "&7/&e" + maxClaims,
                  "",
                  "&aKlicken zum Anzeigen")
            .build());
        
        // Team Info
        long xpForNext = plugin.getConfigManager().getXpForLevel(team.getLevel() + 1);
        inv.setItem(22, new ItemBuilder(Material.BOOK)
            .name("&6Team-Info")
            .lore("&7Level: &e" + team.getLevel(),
                  "&7XP: &e" + team.getXp() + "&7/&e" + xpForNext,
                  "&7Owner: &e" + Bukkit.getOfflinePlayer(team.getOwner()).getName())
            .build());
        
        player.openInventory(inv);
    }
}
