// src/main/java/de/novateams/listeners/GUIListener.java
package de.novateams.listeners;

import de.novateams.NovaTeams;
import de.novateams.gui.*;
import de.novateams.models.Team;
import de.novateams.utils.MessageUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GUIListener implements Listener {
    
    private final NovaTeams plugin;
    
    public GUIListener(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = PlainTextComponentSerializer.plainText()
            .serialize(event.getView().title());
        
        // Backpack - erlaubt Interaktion
        if (title.contains("Team-Backpack")) {
            return; // Keine Einschränkung
        }
        
        // Andere GUIs - Klicks blockieren
        if (title.contains("Team:") || title.contains("Mitglieder") || title.contains("Claims")) {
            event.setCancelled(true);
            
            Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
            if (team == null) {
                player.closeInventory();
                return;
            }
            
            int slot = event.getRawSlot();
            
            // Main Menu Navigation
            if (title.contains("Team:")) {
                switch (slot) {
                    case 11 -> new MembersGUI(plugin, player, team).open();
                    case 13 -> plugin.getBackpackManager().openBackpack(player, team);
                    case 15 -> new ClaimsGUI(plugin, player, team).open();
                }
            }
            
            // Back-Button in Sub-GUIs
            if (slot == event.getInventory().getSize() - 5) { // Center bottom
                if (title.contains("Mitglieder") || title.contains("Claims")) {
                    new TeamMainGUI(plugin, player, team).open();
                }
            }
        }
    }
    
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        // Backpack speichern
        if (plugin.getBackpackManager().isBackpackOpen(player)) {
            plugin.getBackpackManager().saveBackpack(player, event.getInventory());
        }
    }
}
