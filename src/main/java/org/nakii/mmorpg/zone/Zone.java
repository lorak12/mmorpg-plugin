package org.nakii.mmorpg.zone;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Represents a main zone area in the world. A Zone can contain multiple SubZones.
 */
public class Zone {

    protected final String id;
    protected final Component displayName;
    protected final String icon;
    protected final ZoneBounds bounds;
    protected final ZoneFlags flags;
    protected final Map<String, SubZone> subZones;

    public Zone(String id, Component displayName, String icon, ZoneBounds bounds, ZoneFlags flags, Map<String, SubZone> subZones) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.bounds = bounds;
        this.flags = flags;
        this.subZones = subZones;
    }

    /**
     * Recursively finds the most specific Zone or SubZone that contains the given location.
     * It checks its children (SubZones) first before checking itself.
     *
     * @param location The location to check.
     * @return The most specific Zone containing the location, or null if not in this Zone hierarchy.
     */
    @Nullable
    public Zone getZoneForLocation(Location location) {
        if (subZones != null) {
            for (SubZone subZone : subZones.values()) {
                if (subZone.getBounds().contains(location)) {
                    return subZone.getZoneForLocation(location);
                }
            }
        }
        if (this.bounds != null && this.bounds.contains(location)) {
            return this;
        }
        return null;
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

    public Map<String, SubZone> getSubZones() {
        return subZones;
    }
}