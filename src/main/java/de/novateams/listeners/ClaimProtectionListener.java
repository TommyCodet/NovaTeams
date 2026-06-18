// src/main/java/de/novateams/listeners/ClaimProtectionListener.java
package de.novateams.listeners;

import de.novateams.NovaTeams;
import de.novateams.utils.MessageUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClaimProtectionListener implements Listener {
    
    private final NovaTeams plugin;
    
    public ClaimProtectionListener(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("claims.protection.block-break", true)) {
            return;
        }
        
        if (!canInteract(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            MessageUtil.send(event.getPlayer(), "cannot-interact-claim");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("claims.protection.block-place", true)) {
            return;
        }
        
        if (!canInteract(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            MessageUtil.send(event.getPlayer(), "cannot-interact-claim");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        
        var type = event.getClickedBlock().getType();
        var config = plugin.getConfigManager().getConfig();
        
        boolean shouldProtect = false;
        
        // Container
        if (type.name().contains("CHEST") || type.name().contains("BARREL") || 
            type.name().contains("SHULKER") || type.name().contains("HOPPER") ||
            type.name().contains("DISPENSER") || type.name().contains("DROPPER") ||
            type.name().contains("FURNACE") || type.name().contains("SMOKER") ||
            type.name().contains("BLAST")) {
            shouldProtect = config.getBoolean("claims.protection.container-access", true);
        }
        
        // Türen & Gates
        else if (type.name().contains("DOOR") || type.name().contains("GATE") ||
                 type.name().contains("TRAPDOOR")) {
            shouldProtect = config.getBoolean("claims.protection.door-interaction", true);
        }
        
        // Buttons & Lever
        else if (type.name().contains("BUTTON") || type.name().contains("LEVER")) {
            shouldProtect = config.getBoolean("claims.protection.button-lever", true);
        }
        
        // Redstone
        else if (type.name().contains("REPEATER") || type.name().contains("COMPARATOR") ||
                 type.name().contains("REDSTONE")) {
            shouldProtect = config.getBoolean("claims.protection.redstone", true);
        }
        
        if (shouldProtect && !canInteract(event.getPlayer(), event.getClickedBlock().getChunk())) {
            event.setCancelled(true);
            MessageUtil.send(event.getPlayer(), "cannot-interact-claim");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("claims.protection.item-frame", true)) {
            return;
        }
        
        var type = event.getRightClicked().getType();
        if (type.name().contains("ITEM_FRAME")) {
            if (!canInteract(event.getPlayer(), event.getRightClicked().getLocation().getChunk())) {
                event.setCancelled(true);
                MessageUtil.send(event.getPlayer(), "cannot-interact-claim");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("claims.protection.armor-stand", true)) {
            return;
        }
        
        if (!canInteract(event.getPlayer(), event.getRightClicked().getLocation().getChunk())) {
            event.setCancelled(true);
            MessageUtil.send(event.getPlayer(), "cannot-interact-claim");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("claims.protection.entity-damage", true)) {
            return;
        }
        
        if (!(event.getDamager() instanceof Player player)) return;
        
        // Spieler vs Spieler ignorieren
        if (event.getEntity() instanceof Player) return;
        
        if (!canInteract(player, event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
            MessageUtil.send(player, "cannot-interact-claim");
        }
    }
    
    private boolean canInteract(Player player, Chunk chunk) {
        return plugin.getClaimManager().canInteract(player, chunk);
    }
}
