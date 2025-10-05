package org.nakii.mmorpg.util;

import net.kyori.adventure.text.Component;
import org.nakii.mmorpg.MMORPGCore;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChatUtils {


    /**
     * Formats a string with MiniMessage tags into a chat Component.
     * @param text The string to format (e.g., "<red>Hello</red>!")
     * @return A Component ready to be sent to a player.
     */
    public static Component format(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MMORPGCore.getInstance().getMiniMessage().deserialize(text);

    }

    public static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return input;

        String[] words = input.split(" ");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(word.substring(0, 1).toUpperCase());
                result.append(word.substring(1).toLowerCase());
            }
            if (i < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    /**
     * Formats a list of strings, applying MiniMessage formatting to each line.
     * This is primarily used for setting item lore.
     *
     * @param list The list of strings to format.
     * @return A new list containing formatted Components.
     */
    public static List<Component> formatList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        // Use a Java Stream to apply the format() method to every string in the list
        // and collect the results into a new list.
        return list.stream()
                .map(ChatUtils::format)
                .collect(Collectors.toList());
    }
}