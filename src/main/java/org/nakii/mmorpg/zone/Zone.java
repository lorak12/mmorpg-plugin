package org.nakii.mmorpg.zone;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Represents a main zone area in the world. A Zone can contain multiple SubZones.
 */
public class Zone {

    protected final String id;
    protected final Component displayName;
    protected final String icon;
    protected final ZoneBounds bounds;
    protected final ZoneFlags flags;
    private final Location warpPoint;

    public Zone(String id, Component displayName, String icon, ZoneBounds bounds, ZoneFlags flags, @Nullable Location warpPoint) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.bounds = bounds;
        this.flags = flags;
        this.warpPoint = warpPoint;
    }


    /**
     * Gets the effective flags for this zone. For a base Zone, these are just its own configured flags.
     * This method will be overridden by SubZone to handle inheritance.
     *
     * @return The effective ZoneFlags.
     */
    public ZoneFlags getEffectiveFlags() {
        return this.flags;
    }

    public String getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public ZoneBounds getBounds() {
        return bounds;
    }

    public ZoneFlags getFlags() {
        return flags;
    }

    public Optional<Location> getWarpPoint() {
        return Optional.ofNullable(warpPoint);
    }
}