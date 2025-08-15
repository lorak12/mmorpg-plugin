package org.nakii.mmorpg.crafting;

/**
 * A simple, reusable record to represent a single ingredient in a recipe,
 * containing its custom item ID and the required amount.
 */
public record Ingredient(String itemId, int amount) {
    /**
     * Parses a string like "DIAMOND:32" or just "STICK" into an Ingredient object.
     */
    public static Ingredient fromString(String str) {
        if (str == null || str.isEmpty()) return null;
        String[] parts = str.split(":");
        String itemId = parts[0].toUpperCase();
        int amount = (parts.length > 1) ? Integer.parseInt(parts[1]) : 1;
        return new Ingredient(itemId, amount);
    }
}