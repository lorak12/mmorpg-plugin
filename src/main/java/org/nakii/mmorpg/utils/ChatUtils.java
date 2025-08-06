package org.nakii.mmorpg.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public class ChatUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Parses a string with MiniMessage tags into a rich text Component.
     * @param text The string to parse.
     * @return A formatted Component.
     */
    public static Component format(String text) {
        return miniMessage.deserialize(text);
    }

    /**
     * Sends a formatted message to a player or console.
     * @param sender The recipient.
     * @param message The string message with MiniMessage tags.
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }
}