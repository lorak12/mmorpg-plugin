package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.requirements.Requirement;

import java.util.List;
import java.util.logging.Level;

/**
 * A central manager for checking player requirements.
 * This class uses the Requirement factory to parse and evaluate requirement strings.
 */
public class RequirementManager {

    private final MMORPGCore plugin;

    public RequirementManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a player meets all requirements from a given list of strings.
     *
     * @param player The player to check.
     * @param requirementStrings The list of requirement strings from a config file.
     * @return true if the player meets all requirements, false otherwise.
     */
    public boolean meetsAll(Player player, List<String> requirementStrings) {
        if (requirementStrings == null || requirementStrings.isEmpty()) {
            return true; // No requirements means they are met.
        }

        for (String reqString : requirementStrings) {
            Requirement requirement = Requirement.fromString(reqString);

            if (requirement == null) {
                plugin.getLogger().log(Level.WARNING, "Invalid or unrecognized requirement string: '" + reqString + "'");
                continue; // Skip invalid requirements, or you could return false for strictness.
            }

            if (!requirement.meets(player)) {
                return false; // Player failed to meet a requirement.
            }
        }

        return true; // Player meets all requirements.
    }
}