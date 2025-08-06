package org.nakii.mmorpg.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Formats a string with MiniMessage tags into a chat Component.
     * @param text The string to format (e.g., "<red>Hello</red>!")
     * @return A Component ready to be sent to a player.
     */
    public static Component format(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return miniMessage.deserialize(text);
    }

    /**
     * Formats a string with legacy '&' codes into a chat Component.
     * This is useful for compatibility if you still use them somewhere.
     * @param text The string to format (e.g., "&cHello!")
     * @return A Component ready to be sent to a player.
     */
    public static Component formatLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}