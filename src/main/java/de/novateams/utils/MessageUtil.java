// src/main/java/de/novateams/utils/MessageUtil.java
package de.novateams.utils;

import de.novateams.NovaTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MessageUtil {
    
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    
    private MessageUtil() {}
    
    public static void send(CommandSender sender, String key, Object... replacements) {
        var config = NovaTeams.getInstance().getConfigManager().getConfig();
        String prefix = config.getString("messages.prefix", "");
        String message = config.getString("messages." + key, key);
        
        // Replacements anwenden
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("%" + replacements[i] + "%", String.valueOf(replacements[i + 1]));
            }
        }
        
        sender.sendMessage(LEGACY.deserialize(prefix + message));
    }
    
    public static void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(LEGACY.deserialize(message));
    }
    
    public static Component parse(String text) {
        return LEGACY.deserialize(text);
    }
    
    public static String color(String text) {
        return LEGACY.serialize(LEGACY.deserialize(text));
    }
}
