package org.nakii.mmorpg.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BoundingBox;

public class CustomZone {

    private final String id;
    private final World world;
    private final BoundingBox bounds;
    private final ConfigurationSection config;

    public CustomZone(String id, World world, ConfigurationSection config) {
        this.id = id;
        this.world = world;
        this.config = config;

        // Create a BoundingBox for efficient location checking
        double x1 = config.getDouble("pos1.x");
        double y1 = config.getDouble("pos1.y");
        double z1 = config.getDouble("pos1.z");
        double x2 = config.getDouble("pos2.x");
        double y2 = config.getDouble("pos2.y");
        double z2 = config.getDouble("pos2.z");
        this.bounds = new BoundingBox(x1, y1, z1, x2, y2, z2);
    }

    public String getId() {
        return id;
    }

    public World getWorld() {
        return world;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public ConfigurationSection getConfig() {
        return config;
    }

    public boolean contains(Location location) {
        if (!location.getWorld().equals(this.world)) {
            return false;
        }
        return this.bounds.contains(location.toVector());
    }
}