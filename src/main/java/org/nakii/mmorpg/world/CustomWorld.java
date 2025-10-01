package org.nakii.mmorpg.world;

import org.bukkit.World;
import org.bukkit.Location;
import org.nakii.mmorpg.zone.Zone;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single, self-contained gameplay world ("island").
 * It holds the Bukkit World object, its default flags, and a list of all defined zones within it.
 */
public class CustomWorld {

    private final World bukkitWorld;
    private final String displayName;
    private final WorldFlags flags;
    private final List<Zone> zones;
    private final Location spawnPoint;

    public CustomWorld(World bukkitWorld, String displayName, WorldFlags flags, List<Zone> zones, Location spawnPoint) {
        this.bukkitWorld = Objects.requireNonNull(bukkitWorld, "Bukkit World cannot be null");
        this.displayName = displayName;
        this.flags = flags;
        this.zones = zones;
        this.spawnPoint = spawnPoint;
    }

    /**
     * Finds the most specific zone that contains the given location within this world.
     * It iterates through its list of zones to find a match.
     *
     * @param location The location to check.
     * @return The Zone containing the location, or null if the location is in the "wilderness" of this world.
     */
    @Nullable
    public Zone getZoneForLocation(Location location) {
        // Ensure the location is actually in this world before checking zones.
        if (!location.getWorld().equals(this.bukkitWorld)) {
            return null;
        }

        for (Zone zone : zones) {
            // Since we removed sub-zones, this check is now much simpler.
            if (zone.getBounds() != null && zone.getBounds().contains(location)) {
                return zone;
            }
        }
        return null; // Location is in this world but not within any specific zone.
    }

    public World getBukkitWorld() {
        return bukkitWorld;
    }

    public String getDisplayName() {
        return displayName;
    }

    public WorldFlags getFlags() {
        return flags;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public Optional<Location> getSpawnPoint() {
        return Optional.ofNullable(spawnPoint);
    }

    @Nullable
    public Zone getZoneById(String zoneId) {
        return zones.stream().filter(z -> z.getId().equalsIgnoreCase(zoneId)).findFirst().orElse(null);
    }
}