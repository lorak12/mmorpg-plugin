package org.nakii.mmorpg.zone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomZone {
    protected final String id;
    protected final World world;
    protected final Vector min;
    protected final Vector max;
    protected final EnvironmentSettings environment;
    protected final List<MobSpawnInfo> mobs;
    protected final Map<String, CustomSubZone> subZones;

    public CustomZone(String id, ConfigurationSection config) {
        this.id = id;
        this.world = Bukkit.getWorld(config.getString("world", "world"));
        // Get the vectors, but now check if they are null.
        Vector pos1 = getVectorFromConfig(config.getConfigurationSection("pos1"));
        Vector pos2 = getVectorFromConfig(config.getConfigurationSection("pos2"));

        // If either position is missing, we cannot create a valid zone.
        // We will create a "dummy" zone with an impossible bounding box.
        if (pos1 == null || pos2 == null) {
            Bukkit.getLogger().severe("[MMORPGCore] Zone '" + id + "' is missing pos1 or pos2! It will not work correctly.");
            // Set min/max to impossible values so contains() will always be false.
            this.min = new Vector(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            this.max = new Vector(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
        } else {
            // This logic only runs if both positions are valid.
            this.min = new Vector(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
            this.max = new Vector(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
        }

        // Load settings
        if (config.contains("environment")) {
            this.environment = new EnvironmentSettings(config.getConfigurationSection("environment"));
        } else {
            this.environment = null;
        }

        this.mobs = new ArrayList<>();
        if (config.contains("mobs")) {
            for (Map<?, ?> mobMap : config.getMapList("mobs")) {
                ConfigurationSection mobSection = config.createSection("temp_mob", (Map<String, Object>) mobMap);
                this.mobs.add(new MobSpawnInfo(mobSection));
            }
        }

        this.subZones = new HashMap<>();
        if (config.contains("sub-zones")) {
            ConfigurationSection subZoneSection = config.getConfigurationSection("sub-zones");
            for (String subZoneId : subZoneSection.getKeys(false)) {
                this.subZones.put(subZoneId, new CustomSubZone(subZoneId, subZoneSection.getConfigurationSection(subZoneId), this));
            }
        }
    }

    private Vector getVectorFromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        return new Vector(
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z")
        );
    }

    public String getId() { return id; }
    public World getWorld() { return world; }
    public List<MobSpawnInfo> getMobs() { return mobs; }
    public EnvironmentSettings getEnvironment() { return environment; }
    public Map<String, CustomSubZone> getSubZones() { return subZones; }

    public boolean contains(Location loc) {
        if (loc.getWorld() != this.world) {
            return false;
        }
        return loc.getX() >= min.getX() && loc.getX() <= max.getX() &&
                loc.getY() >= min.getY() && loc.getY() <= max.getY() &&
                loc.getZ() >= min.getZ() && loc.getZ() <= max.getZ();
    }
}