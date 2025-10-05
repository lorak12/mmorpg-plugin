package org.nakii.mmorpg.util;

import java.util.HashMap;
import java.util.Map;

public class StringUtils {
    /**
     * Parses a single, simple argument array.
     * Example: ["MY_ITEM", "amount:10"] -> {default=MY_ITEM, amount=10}
     */
    public static Map<String, String> parseArguments(String[] parts, int startIndex) {
        Map<String, String> args = new HashMap<>();
        if (parts.length > startIndex) {
            // Handle the case where the first argument might also be a key:value pair
            if (!parts[startIndex].contains(":")) {
                args.put("default", parts[startIndex]);
                startIndex++;
            }
        }
        for (int i = startIndex; i < parts.length; i++) {
            String[] split = parts[i].split(":", 2);
            if (split.length == 2) {
                args.put(split[0].toLowerCase(), split[1]);
            }
        }
        return args;
    }
}