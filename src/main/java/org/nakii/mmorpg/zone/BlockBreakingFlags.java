package org.nakii.mmorpg.zone;

import org.bukkit.Material;
import java.util.Map;
import java.util.Optional;

/**
 * Holds all configuration related to block breaking and regeneration within a zone.
 * @param unlistedBlocksUnbreakable If true, any block not in the definitions map is unbreakable.
 * @param definitions A map of node IDs to their behavior definitions.
 */
public record BlockBreakingFlags(
        boolean unlistedBlocksUnbreakable,
        Map<String, BlockNode> definitions
) {
    /**
     * Finds the BlockNode corresponding to a given material.
     * @param material The material to look for.
     * @return An Optional containing the found BlockNode, or empty if not found.
     */
    public Optional<BlockNode> findNodeByMaterial(Material material) {
        return definitions.values().stream()
                .filter(node -> node.material() == material)
                .findFirst();
    }
}

