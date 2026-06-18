// src/main/java/de/novateams/hooks/AuraSkillsHook.java
package de.novateams.hooks;

import de.novateams.NovaTeams;
import de.novateams.models.Team;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuraSkillsHook implements Listener {
    
    private final NovaTeams plugin;
    
    public AuraSkillsHook(NovaTeams plugin) {
        this.plugin = plugin;
    }
    
    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        Team team = plugin.getTeamCache().getByPlayer(event.getPlayer().getUniqueId());
        if (team == null) return;
        
        int xp = plugin.getConfigManager().getConfig().getInt("xp.auraskills.level-up", 10);
        plugin.getTeamManager().addXp(team, xp, "AuraSkills Level-Up");
    }
}
