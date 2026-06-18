// src/main/java/de/novateams/commands/TeamTabCompleter.java
package de.novateams.commands;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TeamTabCompleter implements TabCompleter {
    
    private final NovaTeams plugin;
    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "create", "delete", "invite", "join", "leave", "kick",
        "promote", "demote", "info", "backpack", "claim", "unclaim", "map"
    );
    
    public TeamTabCompleter(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        
        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }
        
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            
            return switch (sub) {
                case "invite", "kick", "promote", "demote" -> {
                    Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());
                    if (team == null) yield List.of();
                    
                    if (sub.equals("invite")) {
                        yield filter(Bukkit.getOnlinePlayers().stream()
                            .filter(p -> !team.isMember(p.getUniqueId()))
                            .map(Player::getName)
                            .toList(), args[1]);
                    } else {
                        yield filter(team.getMembers().keySet().stream()
                            .filter(uuid -> !uuid.equals(team.getOwner()))
                            .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                            .toList(), args[1]);
                    }
                }
                case "join", "info" -> filter(plugin.getTeamCache().getAllTeams().stream()
                    .map(Team::getName)
                    .toList(), args[1]);
                default -> List.of();
            };
        }
        
        return List.of();
    }
    
    private List<String> filter(List<String> list, String prefix) {
        return list.stream()
            .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
            .collect(Collectors.toList());
    }
}
