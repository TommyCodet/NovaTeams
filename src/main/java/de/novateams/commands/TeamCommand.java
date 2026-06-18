// src/main/java/de/novateams/commands/TeamCommand.java
package de.novateams.commands;

import de.novateams.NovaTeams;
import de.novateams.gui.TeamMainGUI;
import de.novateams.managers.ClaimManager;
import de.novateams.models.Team;
import de.novateams.models.TeamRole;
import de.novateams.utils.ChunkKey;
import de.novateams.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamCommand implements CommandExecutor {
    
    private final NovaTeams plugin;
    
    public TeamCommand(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl ist nur für Spieler.");
            return true;
        }
        
        if (!player.hasPermission("novateams.use")) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        if (args.length == 0) {
            // GUI öffnen, wenn in Team
            Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
            if (team != null) {
                new TeamMainGUI(plugin, player, team).open();
            } else {
                sendHelp(player);
            }
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        return switch (sub) {
            case "create" -> handleCreate(player, args);
            case "delete" -> handleDelete(player);
            case "invite" -> handleInvite(player, args);
            case "join" -> handleJoin(player, args);
            case "leave" -> handleLeave(player);
            case "kick" -> handleKick(player, args);
            case "promote" -> handlePromote(player, args);
            case "demote" -> handleDemote(player, args);
            case "info" -> handleInfo(player, args);
            case "backpack", "bp" -> handleBackpack(player);
            case "claim" -> handleClaim(player);
            case "unclaim" -> handleUnclaim(player);
            case "map" -> handleMap(player);
            default -> {
                sendHelp(player);
                yield true;
            }
        };
    }
    
    private boolean handleCreate(Player player, String[] args) {
        if (plugin.getTeamCache().getByPlayer(player.getUniqueId()) != null) {
            MessageUtil.send(player, "already-in-team");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendRaw(player, "&cVerwendung: /team create <name>");
            return true;
        }
        
        String name = args[1];
        int minLen = plugin.getConfigManager().getConfig().getInt("team.min-name-length", 3);
        int maxLen = plugin.getConfigManager().getConfig().getInt("team.max-name-length", 16);
        
        if (name.length() < minLen || name.length() > maxLen) {
            MessageUtil.sendRaw(player, "&cDer Teamname muss zwischen " + minLen + " und " + maxLen + " Zeichen lang sein.");
            return true;
        }
        
        if (plugin.getTeamCache().existsByName(name)) {
            MessageUtil.send(player, "team-already-exists");
            return true;
        }
        
        Team team = plugin.getTeamManager().createTeam(name, player);
        if (team != null) {
            MessageUtil.send(player, "team-created", "team", name);
        }
        
        return true;
    }
    
    private boolean handleDelete(Player player) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        if (!team.getOwner().equals(player.getUniqueId())) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        // Alle Mitglieder benachrichtigen
        team.getMembers().keySet().forEach(uuid -> {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null) {
                MessageUtil.send(member, "team-deleted");
            }
        });
        
        plugin.getTeamManager().deleteTeam(team);
        return true;
    }
    
    private boolean handleInvite(Player player, String[] args) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        TeamRole role = team.getRole(player.getUniqueId());
        if (!role.isAtLeast(TeamRole.MODERATOR)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendRaw(player, "&cVerwendung: /team invite <spieler>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtil.sendRaw(player, "&cSpieler nicht gefunden.");
            return true;
        }
        
        if (plugin.getTeamCache().getByPlayer(target.getUniqueId()) != null) {
            MessageUtil.sendRaw(player, "&cDieser Spieler ist bereits in einem Team.");
            return true;
        }
        
        team.addInvite(target.getUniqueId());
        MessageUtil.send(player, "player-invited", "player", target.getName());
        MessageUtil.send(target, "invite-received", "team", team.getName());
        
        // Timeout für Einladung
        int timeout = plugin.getConfigManager().getConfig().getInt("team.invite-timeout", 300);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            team.removeInvite(target.getUniqueId());
        }, timeout * 20L);
        
        return true;
    }
    
    private boolean handleJoin(Player player, String[] args) {
        if (plugin.getTeamCache().getByPlayer(player.getUniqueId()) != null) {
            MessageUtil.send(player, "already-in-team");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendRaw(player, "&cVerwendung: /team join <teamname>");
            return true;
        }
        
        Team team = plugin.getTeamCache().getByName(args[1]);
        if (team == null) {
            MessageUtil.send(player, "team-not-found");
            return true;
        }
        
        if (!team.hasInvite(player.getUniqueId())) {
            MessageUtil.sendRaw(player, "&cDu hast keine Einladung zu diesem Team.");
            return true;
        }
        
        team.removeInvite(player.getUniqueId());
        team.addMember(player.getUniqueId(), TeamRole.MEMBER);
        plugin.getTeamCache().updatePlayerTeam(player.getUniqueId(), team.getId());
        
        MessageUtil.send(player, "joined-team", "team", team.getName());
        
        return true;
    }
    
    private boolean handleLeave(Player player) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        if (team.getOwner().equals(player.getUniqueId())) {
            MessageUtil.sendRaw(player, "&cAls Owner musst du das Team löschen oder einen neuen Owner ernennen.");
            return true;
        }
        
        team.removeMember(player.getUniqueId());
        plugin.getTeamCache().updatePlayerTeam(player.getUniqueId(), null);
        MessageUtil.send(player, "left-team");
        
        return true;
    }
    
    private boolean handleKick(Player player, String[] args) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        TeamRole role = team.getRole(player.getUniqueId());
        if (!role.isAtLeast(TeamRole.MODERATOR)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendRaw(player, "&cVerwendung: /team kick <spieler>");
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!team.isMember(target.getUniqueId())) {
            MessageUtil.sendRaw(player, "&cDieser Spieler ist nicht in deinem Team.");
            return true;
        }
        
        TeamRole targetRole = team.getRole(target.getUniqueId());
        if (!plugin.getTeamManager().canManageRole(role, targetRole)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        team.removeMember(target.getUniqueId());
        plugin.getTeamCache().updatePlayerTeam(target.getUniqueId(), null);
        MessageUtil.send(player, "player-kicked", "player", target.getName());
        
        if (target.isOnline()) {
            MessageUtil.sendRaw((Player) target, "&cDu wurdest aus dem Team gekickt.");
        }
        
        return true;
    }
    
    private boolean handlePromote(Player player, String[] args) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        TeamRole role = team.getRole(player.getUniqueId());
        if (!role.isAtLeast(TeamRole.CO_OWNER)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendRaw(player, "&cVerwendung: /team promote <spieler>");
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!team.isMember(target.getUniqueId())) {
            MessageUtil.sendRaw(player, "&cDieser Spieler ist nicht in deinem Team.");
            return true;
        }
        
        TeamRole targetRole = team.getRole(target.getUniqueId());
        if (!plugin.getTeamManager().canPromote(role, targetRole)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        TeamRole newRole = targetRole.getNextHigher();
        team.setRole(target.getUniqueId(), newRole);
        MessageUtil.send(player, "player-promoted", "player", target.getName(), "role", newRole.getDisplayName());
        
        return true;
    }
    
    private boolean handleDemote(Player player, String[] args) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        TeamRole role = team.getRole(player.getUniqueId());
        if (!role.isAtLeast(TeamRole.CO_OWNER)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            MessageUtil.sendRaw(player, "&cVerwendung: /team demote <spieler>");
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!team.isMember(target.getUniqueId())) {
            MessageUtil.sendRaw(player, "&cDieser Spieler ist nicht in deinem Team.");
            return true;
        }
        
        TeamRole targetRole = team.getRole(target.getUniqueId());
        if (!plugin.getTeamManager().canManageRole(role, targetRole)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        TeamRole newRole = targetRole.getNextLower();
        team.setRole(target.getUniqueId(), newRole);
        MessageUtil.send(player, "player-demoted", "player", target.getName(), "role", newRole.getDisplayName());
        
        return true;
    }
    
    private boolean handleInfo(Player player, String[] args) {
        Team team;
        
        if (args.length >= 2) {
            team = plugin.getTeamCache().getByName(args[1]);
            if (team == null) {
                MessageUtil.send(player, "team-not-found");
                return true;
            }
        } else {
            team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
            if (team == null) {
                MessageUtil.sendRaw(player, "&cVerwendung: /team info <teamname> oder sei in einem Team.");
                return true;
            }
        }
        
        long xpForNext = plugin.getConfigManager().getXpForLevel(team.getLevel() + 1);
        int maxClaims = plugin.getConfigManager().getMaxClaims(team.getLevel());
        int currentClaims = plugin.getClaimCache().getTeamClaimCount(team.getId());
        
        MessageUtil.sendRaw(player, "&8&m----------------------------------------");
        MessageUtil.sendRaw(player, "&6&l" + team.getName() + " &7- Team Info");
        MessageUtil.sendRaw(player, "&8&m----------------------------------------");
        MessageUtil.sendRaw(player, "&7Level: &e" + team.getLevel());
        MessageUtil.sendRaw(player, "&7XP: &e" + team.getXp() + "&7/&e" + xpForNext);
        MessageUtil.sendRaw(player, "&7Mitglieder: &e" + team.getMemberCount());
        MessageUtil.sendRaw(player, "&7Claims: &e" + currentClaims + "&7/&e" + maxClaims);
        MessageUtil.sendRaw(player, "&7Owner: &e" + Bukkit.getOfflinePlayer(team.getOwner()).getName());
        MessageUtil.sendRaw(player, "&8&m----------------------------------------");
        
        return true;
    }
    
    private boolean handleBackpack(Player player) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        plugin.getBackpackManager().openBackpack(player, team);
        return true;
    }
    
    private boolean handleClaim(Player player) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        TeamRole role = team.getRole(player.getUniqueId());
        if (!role.isAtLeast(TeamRole.MODERATOR)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        Chunk chunk = player.getLocation().getChunk();
        ClaimManager.ClaimResult result = plugin.getClaimManager().claim(chunk, player, team);
        
        switch (result) {
            case SUCCESS -> MessageUtil.send(player, "chunk-claimed");
            case ALREADY_CLAIMED -> MessageUtil.send(player, "chunk-already-claimed");
            case ALREADY_OWNED -> MessageUtil.sendRaw(player, "&7Dieser Chunk gehört bereits deinem Team.");
            case MAX_REACHED -> MessageUtil.send(player, "max-claims-reached");
        }
        
        return true;
    }
    
    private boolean handleUnclaim(Player player) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        if (team == null) {
            MessageUtil.send(player, "not-in-team");
            return true;
        }
        
        TeamRole role = team.getRole(player.getUniqueId());
        if (!role.isAtLeast(TeamRole.MODERATOR)) {
            MessageUtil.send(player, "no-permission");
            return true;
        }
        
        Chunk chunk = player.getLocation().getChunk();
        boolean success = plugin.getClaimManager().unclaim(chunk, team);
        
        if (success) {
            MessageUtil.send(player, "chunk-unclaimed");
        } else {
            MessageUtil.send(player, "chunk-not-claimed");
        }
        
        return true;
    }
    
    private boolean handleMap(Player player) {
        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
        Chunk center = player.getLocation().getChunk();
        
        MessageUtil.sendRaw(player, "&8&m----------------------------------------");
        MessageUtil.sendRaw(player, "&6Claim-Map &7(Du bist in der Mitte)");
        MessageUtil.sendRaw(player, "");
        
        StringBuilder sb = new StringBuilder();
        for (int dz = -4; dz <= 4; dz++) {
            sb.setLength(0);
            sb.append("&7");
            for (int dx = -8; dx <= 8; dx++) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                ChunkKey key = new ChunkKey(center.getWorld().getName(), x, z);
                
                UUID claimedBy = plugin.getClaimCache().getTeamId(key);
                
                if (dx == 0 && dz == 0) {
                    sb.append("&c█");
                } else if (claimedBy == null) {
                    sb.append("&7░");
                } else if (team != null && claimedBy.equals(team.getId())) {
                    sb.append("&a█");
                } else {
                    sb.append("&e█");
                }
            }
            MessageUtil.sendRaw(player, sb.toString());
        }
        
        MessageUtil.sendRaw(player, "");
        MessageUtil.sendRaw(player, "&cDu &7| &aTeam &7| &eAndere &7| &7░Frei");
        MessageUtil.sendRaw(player, "&8&m----------------------------------------");
        
        return true;
    }
    
    private void sendHelp(Player player) {
        MessageUtil.sendRaw(player, "&8&m----------------------------------------");
        MessageUtil.sendRaw(player, "&6&lNovaTeams &7- Hilfe");
        MessageUtil.sendRaw(player, "&8&m----------------------------------------");
        MessageUtil.sendRaw(player, "&e/team create <name> &7- Team erstellen");
        MessageUtil.sendRaw(player, "&e/team delete &7- Team löschen");
        MessageUtil.sendRaw(player, "&e/team invite <spieler> &7- Spieler einladen");
        MessageUtil.sendRaw(player, "&e/team join <team> &7- Team beitreten");
        MessageUtil.sendRaw(player, "&e/team leave &7- Team verlassen");
        MessageUtil.sendRaw(player, "&e/team kick <spieler> &7- Spieler kicken");
        MessageUtil.sendRaw(player, "&e/team promote <spieler> &7- Befördern");
        MessageUtil.sendRaw(player, "&e/team demote <spieler> &7- Degradieren");
        MessageUtil.sendRaw(player, "&e/team info [team] &7- Team-Info");
        MessageUtil.sendRaw(player, "&e/team backpack &7- Team-Backpack");
        MessageUtil.sendRaw(player, "&e/team claim &7- Chunk claimen");
        MessageUtil.sendRaw(player, "&e/team unclaim &7- Claim entfernen");
        MessageUtil.sendRaw(player, "&e/team map &7- Claim-Map");
        MessageUtil.sendRaw(player, "&8&m----------------------------------------");
    }
}
