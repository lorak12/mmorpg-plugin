package org.nakii.mmorpg.world;

import java.util.Collections;
import java.util.List;

/**
 * Represents the default gameplay rules and flags for an entire CustomWorld.
 * These are loaded from the world.yml file.
 *
 * @param canPlaceBlocks Determines if players can place blocks.
 * @param canBreakBlocks Determines if players can break blocks.
 * @param pvpMode The PvP policy ("false", "true", "faction-only").
 * @param forcePeaceful If true, no hostile mobs will spawn or target players naturally.
 * @param entryRequirements A list of requirements needed to enter this world.
 */
public record WorldFlags(
        boolean canPlaceBlocks,
        boolean canBreakBlocks,
        String pvpMode,
        boolean forcePeaceful,
        List<String> entryRequirements
) {
    /**
     * A default, empty set of flags for worlds that might be missing a configuration.
     */
    public static final WorldFlags EMPTY = new WorldFlags(
            false,
            false,
            "false",
            false,
            Collections.emptyList()
    );
}