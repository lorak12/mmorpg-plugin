package org.nakii.mmorpg.zone;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents the geometric boundaries of a Zone.
 * This uses a 2D polygon for the X/Z plane and a min/max Y for height,
 * allowing for complex, non-rectangular shapes.
 */
public class ZoneBounds {

    private final Path2D.Double shape;
    private final double minY;
    private final double maxY;

    public ZoneBounds(List<Point2D.Double> points, double minY, double maxY) {
        this.minY = minY;
        this.maxY = maxY;

        // Construct the 2D polygon shape from the list of points
        this.shape = new Path2D.Double();
        if (points != null && !points.isEmpty()) {
            shape.moveTo(points.get(0).getX(), points.get(0).getY());
            for (int i = 1; i < points.size(); i++) {
                shape.lineTo(points.get(i).getX(), points.get(i).getY());
            }
            shape.closePath();
        }
    }

    /**
     * Checks if a given Bukkit Location is within these bounds.
     * The check is performed first on the Y-axis for efficiency, then on the X/Z plane.
     *
     * @param location The location to check.
     * @return True if the location is inside the bounds, false otherwise.
     */
    public boolean contains(Location location) {
        if (location.getY() < minY || location.getY() > maxY) {
            return false;
        }
        // The Path2D shape contains method checks the X and Z coordinates
        return shape.contains(location.getX(), location.getZ());
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    /**
     * Gets a collection of entities that are physically within the zone's cuboid bounds.
     * Note: This is a broad-phase check and does not check the polygonal shape.
     * @return A collection of nearby entities.
     */
    public Collection<Entity> getNearbyEntities() {
        // You'll need to figure out the world and center point.
        // This is a simplification. A real implementation needs access to the world object.
        // Let's assume you have a method to get the world and a center point.
        World world = Bukkit.getWorld("world"); // This needs to be dynamic
        if (world == null) return Collections.emptyList();

        // This is an approximation. A more accurate method might be needed.
        BoundingBox box = new BoundingBox(getMinX(), minY, getMinZ(), getMaxX(), maxY, getMaxZ());
        return world.getNearbyEntities(box);
    }

    /**
     * Tries to find a random, safe location for a mob to spawn within the zone bounds.
     * "Safe" means a solid block below and two air blocks for the mob.
     * @param random A Random instance.
     * @return A safe Location, or null if one couldn't be found after several tries.
     */
    public Location getRandomSafeLocation(Random random) {
        // This requires access to the world and bounding box corners
        World world = Bukkit.getWorld("world"); // Again, needs to be dynamic
        if (world == null) return null;

        int minX = (int) getMinX();
        int minZ = (int) getMinZ();
        int xRange = (int) (getMaxX() - minX);
        int zRange = (int) (getMaxZ() - minZ);

        for (int i = 0; i < 10; i++) { // Try 10 times to find a spot
            int x = minX + random.nextInt(xRange);
            int z = minZ + random.nextInt(zRange);

            // Check if this random point is actually inside the polygon
            if (shape.contains(x, z)) {
                // Find the highest solid block at this X,Z
                Block ground = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                Location potentialSpawn = ground.getLocation().add(0.5, 1, 0.5);

                // Check if the location is within Y-bounds and is safe
                if (potentialSpawn.getY() >= minY && potentialSpawn.getY() <= maxY && potentialSpawn.getBlock().isPassable() && potentialSpawn.clone().add(0, 1, 0).getBlock().isPassable()) {
                    return potentialSpawn;
                }
            }
        }
        return null; // Failed to find a safe location
    }

    // You'll need to add methods to get the min/max X and Z of the polygon
    private double getMinX() { return shape.getBounds2D().getMinX(); }
    private double getMaxX() { return shape.getBounds2D().getMaxX(); }
    private double getMinZ() { return shape.getBounds2D().getMinY(); } // minY of a Path2D is the minZ in Minecraft
    private double getMaxZ() { return shape.getBounds2D().getMaxY(); } // maxY of a Path2D is the maxZ in Minecraft
}