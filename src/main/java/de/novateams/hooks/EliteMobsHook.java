package de.novateams.hooks;

import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import de.novateams.NovaTeams;
import de.novateams.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EliteMobsHook implements Listener {

    private final NovaTeams plugin;

    public EliteMobsHook(NovaTeams plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEliteMobDeath(EliteMobDeathEvent event) {

        if (!(event.getEliteEntity().getDamagers().getTopDamager() instanceof Player player)) {
            return;
        }

        Team team = plugin.getTeamCache().getByPlayer(player.getUniqueId());

        if (team == null) {
            return;
        }

        int level = event.getEliteEntity().getLevel();

        int xpPerLevel = plugin.getConfigManager()
                .getConfig()
                .getInt("xp.elitemobs.boss-kill-per-level", 5);

        int totalXp = level * xpPerLevel;

        plugin.getTeamManager().addXp(team, totalXp, "EliteMobs Boss");
    }
}
