package org.nakii.mmorpg.requirements;

import org.bukkit.entity.Player;

/**
 * An interface representing a single condition a player must meet to use an item.
 */
@FunctionalInterface
public interface Requirement {
    /**
     * Checks if the player meets this requirement.
     * @param player The player to check.
     * @return True if the player meets the requirement, false otherwise.
     */
    boolean meets(Player player);

    /**
     * A static factory method to parse a requirement string into a Requirement object.
     * @param requirementString The string from the YAML config (e.g., "SKILL:COMBAT:20").
     * @return A Requirement object, or null if the string is invalid.
     */
    static Requirement fromString(String requirementString) {
        String[] parts = requirementString.split(":");
        if (parts.length < 3) return null;

        String type = parts[0].toUpperCase();
        String context = parts[1].toUpperCase();
        int value = Integer.parseInt(parts[2]);

        return switch (type) {
            case "SKILL" -> new SkillRequirement(context, value);
            case "SLAYER" -> new SlayerRequirement(context, value);
            case "COLLECTION" -> new CollectionRequirement(context, value);

            default -> null;
        };
    }
}