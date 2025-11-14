package org.nakii.mmorpg.requirements;

import org.bukkit.entity.Player;

/**
 * A marker interface representing a single condition a player must meet.
 * The logic for checking the requirement is handled by the RequirementManager.
 */
public interface Requirement {

    /**
     * A static factory method to parse a requirement string into a Requirement data object.
     * @param requirementString The string from the YAML config (e.g., "SKILL:COMBAT:20").
     * @return A Requirement object, or null if the string is invalid.
     */
    static Requirement fromString(String requirementString) {
        String[] parts = requirementString.split(":");
        if (parts.length < 3) return null;

        String type = parts[0].toUpperCase();
        String context = parts[1].toUpperCase();
        int value;
        try {
            value = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return null; // Invalid number format
        }

        return switch (type) {
            case "SKILL" -> new SkillRequirement(context, value);
            case "SLAYER" -> new SlayerRequirement(context, value);
            case "COLLECTION" -> new CollectionRequirement(context, value);
            default -> null;
        };
    }
}