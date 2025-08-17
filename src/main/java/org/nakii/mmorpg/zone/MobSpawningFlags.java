package org.nakii.mmorpg.zone;

import org.bukkit.Location;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Holds configuration related to mob spawning within a zone.
 * @param spawnCap The maximum number of these mobs allowed in the zone.
 * @param mobs A map of Mob IDs to their spawn weight.
 * @param spawnerPoints A list of specific locations where mobs can spawn.
 */
public record MobSpawningFlags(
        int spawnCap,
        Map<String, Integer> mobs,
        List<Location> spawnerPoints
) {
    /**
     * Selects a random mob ID from the weighted list.
     * @param random A Random instance.
     * @return The ID of the chosen mob, or null if the list is empty.
     */
    public String getRandomMob(Random random) {
        if (mobs == null || mobs.isEmpty()) {
            return null;
        }

        int totalWeight = mobs.values().stream().mapToInt(Integer::intValue).sum();
        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (roll < cumulativeWeight) {
                return entry.getKey();
            }
        }
        return null; // Should not be reached if totalWeight > 0
    }
}