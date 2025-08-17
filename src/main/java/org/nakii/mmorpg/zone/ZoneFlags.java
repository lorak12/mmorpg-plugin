package org.nakii.mmorpg.zone;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A container for all configurable properties (flags) of a zone.
 * Using records provides immutability and conciseness.
 */
public record ZoneFlags(
        List<String> entryRequirements,
        Climate climate,
        Map<String, Double> passiveStats,
        PlayerVisualFlags playerVisualFlags,
        BlockBreakingFlags blockBreakingFlags,
        MobSpawningFlags mobSpawningFlags // ADD THIS LINE
) {
    public static final ZoneFlags EMPTY = new ZoneFlags(
            Collections.emptyList(),
            null,
            Collections.emptyMap(),
            null,
            null,
            null // ADD THIS LINE
    );
}


