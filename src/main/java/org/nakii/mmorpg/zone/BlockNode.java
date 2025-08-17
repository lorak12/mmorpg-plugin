package org.nakii.mmorpg.zone;

import org.bukkit.Material; /**
 * Represents a single state in a block regeneration cycle.
 * @param id The unique identifier for this node (e.g., "STONE_NODE").
 * @param material The Bukkit Material for this state.
 * @param breaksTo The ID of the node this block turns into when broken. Can be null.
 * @param revertsTo The ID of the node this block reverts to over time. Can be null.
 * @param revertTimeSeconds The time in seconds it takes to revert. 0 if not applicable.
 */
public record BlockNode(
        String id,
        Material material,
        String breaksTo,
        String revertsTo,
        long revertTimeSeconds
) {}
