package org.nakii.mmorpg.util;

import net.kyori.adventure.text.Component;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.Stat;

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

    /**
     * Formats a list of strings, applying MiniMessage formatting to each line.
     */
    public static List<Component> formatList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(ChatUtils::format)
                .collect(Collectors.toList());
    }

    public static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.split("_");
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
     * The single source of truth for formatting any reward string from a config file.
     * Use this in GUIs, chat messages, and anywhere else rewards are displayed.
     * @param rewardString The raw string from the config (e.g., "SKILL_XP:MINING:1500").
     * @return A formatted, user-friendly MiniMessage string with icons.
     */
    public static String formatRewardString(String rewardString) {
        String[] parts = rewardString.split(":");
        if (parts.length < 2) return "<gray>• " + rewardString.replace("_", " ");

        String type = parts[0].toUpperCase();
        String context = parts[1];

        try {
            return switch (type) {
                case "COINS" -> "<gray>• <gold>⛀ +" + String.format("%,d", Integer.parseInt(context)) + " Coins</gold>";
                case "SKILL_XP" -> {
                    if (parts.length < 3) yield "<red>Invalid Skill XP Reward</red>";
                    String skillName = capitalizeWords(context);
                    yield "<gray>• <aqua>☯ +" + String.format("%,d", Integer.parseInt(parts[2])) + " " + skillName + " XP</aqua>";
                }
                case "STAT_BOOST" -> {
                    if (parts.length < 3) yield "<red>Invalid Stat Boost Reward</red>";
                    Stat stat = Stat.valueOf(context.toUpperCase());
                    double value = Double.parseDouble(parts[2]);
                    yield "<gray>• " + stat.format(value);
                }
                case "RECIPE_UNLOCK" -> "<gray>• <green>⚒ New Recipe:</green> <white>" + capitalizeWords(context) + "</white>";
                case "ACCESS_UNLOCK" -> "<gray>• <aqua>✈ Area Unlock:</aqua> <white>" + capitalizeWords(context) + "</white>";
                case "TRADE_UNLOCK" -> "<gray>• <yellow>§ Trade Unlock:</yellow> <white>" + capitalizeWords(context) + "</white>";
                case "ITEM_GIVE" -> {
                    if (parts.length < 3) yield "<red>Invalid Item Reward</red>";
                    yield "<gray>• <blue>✙ " + capitalizeWords(context) + " x" + parts[2] + "</blue>";
                }
                default -> "<gray>• " + rewardString.replace("_", " ");
            };
        } catch (Exception e) {
            // Catch potential errors from parseInt or valueOf
            return "<red>Invalid Reward: " + rewardString + "</red>";
        }
    }
}