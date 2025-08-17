package org.nakii.mmorpg.zone;

import net.kyori.adventure.text.Component;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a zone that is a child of another Zone or SubZone.
 */
public class SubZone extends Zone {

    private final Zone parent;

    public SubZone(String id, Component displayName, String icon, ZoneBounds bounds, ZoneFlags flags, Map<String, SubZone> subZones, Zone parent) {
        super(id, displayName, icon, bounds, flags, subZones);
        this.parent = parent;
    }

    public Zone getParent() {
        return parent;
    }

    /**
     * Calculates the effective flags for this zone by inheriting from its parent.
     * This is where the "Principle of Specificity" is implemented. If a flag is
     * defined in this SubZone, it is used; otherwise, the parent's effective flag is used.
     * This is a deep merge of the flags.
     *
     * @return The merged ZoneFlags.
     */
    @Override
    public ZoneFlags getEffectiveFlags() {
        ZoneFlags parentFlags = parent.getEffectiveFlags();
        ZoneFlags myFlags = this.flags;

        // If this zone has no specific flags defined, just return the parent's effective flags.
        if (myFlags == null) {
            return parentFlags != null ? parentFlags : ZoneFlags.EMPTY;
        }

        // If the parent has no flags, just return this zone's flags.
        if (parentFlags == null) {
            return myFlags;
        }

        // --- Perform a Deep Merge ---
        // If 'myFlags' has a specific property set (not null), use it.
        // Otherwise, fall back to the property from 'parentFlags'.

        var effectiveEntryReqs = myFlags.entryRequirements() != null ? myFlags.entryRequirements() : parentFlags.entryRequirements();
        var effectiveClimate = myFlags.climate() != null ? myFlags.climate() : parentFlags.climate();
        var effectivePlayerFlags = myFlags.playerVisualFlags() != null ? myFlags.playerVisualFlags() : parentFlags.playerVisualFlags();
        var effectiveBlockFlags = myFlags.blockBreakingFlags() != null ? myFlags.blockBreakingFlags() : parentFlags.blockBreakingFlags();
        var effectiveMobFlags = myFlags.mobSpawningFlags() != null ? myFlags.mobSpawningFlags() : parentFlags.mobSpawningFlags();

        // For maps like passiveStats, we should merge them. Child stats overwrite parent stats with the same key.
        Map<String, Double> mergedPassiveStats = Stream.concat(
                parentFlags.passiveStats().entrySet().stream(),
                myFlags.passiveStats().entrySet().stream()
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (valueFromChild, valueFromParent) -> valueFromChild // Child's value wins on conflict
        ));

        return new ZoneFlags(
                effectiveEntryReqs,
                effectiveClimate,
                mergedPassiveStats,
                effectivePlayerFlags,
                effectiveBlockFlags,
                effectiveMobFlags // ADD TO RETURN
        );
    }
}