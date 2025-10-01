package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.zone.*;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * REFACTORED: This class is no longer a global manager.
 * Its purpose is now to act as a utility for parsing a zones.yml file
 * and returning a list of Zone objects. It is now stateless.
 */
public class ZoneManager {

    private final MMORPGCore plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ZoneManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * NEW: Parses a single zones.yml file and returns a list of the zones defined within it.
     * This method is called by the WorldManager for each custom world.
     *
     * @param zoneFile The zones.yml file to parse.
     * @return A List of Zone objects. Returns an empty list if the file doesn't exist or is invalid.
     */
    public List<Zone> loadZonesFromFile(File zoneFile, World worldContext) {
        List<Zone> zones = new ArrayList<>();
        if (!zoneFile.exists()) {
            return zones;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(zoneFile);
        ConfigurationSection zonesSection = config.getConfigurationSection("zones");
        if (zonesSection == null) {
            return zones;
        }

        for (String zoneId : zonesSection.getKeys(false)) {
            try {
                ConfigurationSection zoneConfig = zonesSection.getConfigurationSection(zoneId);
                if (zoneConfig != null) {
                    // --- PASS WORLD CONTEXT DOWN ---
                    Zone zone = parseZone(zoneId, zoneConfig, null, worldContext);
                    if (zone != null) {
                        zones.add(zone);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load zone '" + zoneId + "' from file: " + zoneFile.getName(), e);
            }
        }
        return zones;
    }

    // --- All private parsing logic below remains the same, as it is still needed ---

    private Zone parseZone(String id, ConfigurationSection config, @Nullable Zone parent, World worldContext) {
        var displayName = miniMessage.deserialize(config.getString("display-name", "<red>Unnamed Zone"));
        String icon = config.getString("icon", "❓");
        ZoneBounds bounds = parseBounds(config.getConfigurationSection("bounds"));
        ConfigurationSection flagsSection = config.getConfigurationSection("flags");
        ZoneFlags flags = parseFlags(flagsSection, worldContext);

        // We now parse the warp-point and pass it to the constructor.
        Location warpPoint = parseLocation(worldContext, config.getString("warp-point"));

        // This now correctly calls the constructor with all 6 required arguments.
        return new Zone(id, displayName, icon, bounds, flags, warpPoint);
    }

    private ZoneBounds parseBounds(ConfigurationSection config) {
        if (config == null) return new ZoneBounds(Collections.emptyList(), -64, 320);
        double minY = config.getDouble("min-y", -64.0);
        double maxY = config.getDouble("max-y", 320.0);
        List<String> pointsList = config.getStringList("points");
        List<Point2D.Double> points = new ArrayList<>();
        for (String pointStr : pointsList) {
            String[] parts = pointStr.split(",");
            if (parts.length == 2) {
                try {
                    double x = Double.parseDouble(parts[0].trim());
                    double z = Double.parseDouble(parts[1].trim());
                    points.add(new Point2D.Double(x, z));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid point format in bounds: '" + pointStr + "'");
                }
            }
        }
        return new ZoneBounds(points, minY, maxY);
    }

    private ZoneFlags parseFlags(ConfigurationSection config, World worldContext) {
        if (config == null) {
            return ZoneFlags.EMPTY;
        }
        List<String> entryReqs = config.getStringList("entry-requirements");
        Climate climate = null;
        if (config.isConfigurationSection("climate")) {
            ConfigurationSection climateConfig = config.getConfigurationSection("climate");
            climate = new Climate(
                    climateConfig.getString("type", "NEUTRAL"),
                    climateConfig.getInt("required-resistance", 0)
            );
        }
        BlockBreakingFlags blockBreakingFlags = parseBlockBreakingFlags(config.getConfigurationSection("block-breaking"));
        // --- PASS WORLD CONTEXT DOWN ---
        MobSpawningFlags mobSpawningFlags = parseMobSpawningFlags(config.getConfigurationSection("mob-spawning"), worldContext);
        return new ZoneFlags(entryReqs, climate, Collections.emptyMap(), null, blockBreakingFlags, mobSpawningFlags);
    }

    private BlockBreakingFlags parseBlockBreakingFlags(ConfigurationSection config) {
        // This method's implementation remains unchanged from your original file
        if (config == null) return null;
        boolean unlistedUnbreakable = config.getBoolean("unlisted-blocks-unbreakable", false);
        Map<String, BlockNode> definitions = new HashMap<>();
        ConfigurationSection defsSection = config.getConfigurationSection("definitions");
        if (defsSection != null) {
            for (String nodeId : defsSection.getKeys(false)) {
                ConfigurationSection nodeConfig = defsSection.getConfigurationSection(nodeId);
                if (nodeConfig != null) {
                    try {
                        Material material = Material.valueOf(nodeConfig.getString("material", "").toUpperCase());
                        String breaksTo = nodeConfig.getString("breaks-to");
                        String revertsToStr = nodeConfig.getString("reverts-to");
                        String revertsTo = null;
                        long revertTime = 0;
                        if (revertsToStr != null && revertsToStr.contains(":")) {
                            String[] parts = revertsToStr.split(":");
                            revertsTo = parts[0];
                            revertTime = Long.parseLong(parts[1].replace("s", ""));
                        }
                        String customDropId = nodeConfig.getString("custom-drop-id");
                        String collectionId = nodeConfig.getString("collection-id");
                        int breakingPower = nodeConfig.getInt("breaking-power-required", 0);
                        double breakTime = nodeConfig.getDouble("base-break-time-seconds", 0.0);
                        double skillXp = nodeConfig.getDouble("skill-xp-reward", 0.0);
                        String skillType = nodeConfig.getString("skill-type");
                        definitions.put(nodeId, new BlockNode(nodeId, material, breaksTo, revertsTo, revertTime,
                                customDropId, collectionId, breakingPower, breakTime, skillXp, skillType));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material in block-breaking definition for node '" + nodeId + "'");
                    }
                }
            }
        }
        return new BlockBreakingFlags(unlistedUnbreakable, definitions);
    }

    private MobSpawningFlags parseMobSpawningFlags(ConfigurationSection config, World worldContext) {
        if (config == null) return null;
        int spawnCap = config.getInt("spawn-cap", 0);
        if (spawnCap <= 0) return null;
        Map<String, Integer> mobs = new HashMap<>();
        ConfigurationSection mobsSection = config.getConfigurationSection("mobs");
        if (mobsSection != null) {
            for (String mobId : mobsSection.getKeys(false)) {
                mobs.put(mobId, mobsSection.getInt(mobId, 1));
            }
        }
        List<Location> spawnerPoints = new ArrayList<>();
        List<String> pointsList = config.getStringList("spawner-points");

        for (String pointStr : pointsList) {
            String[] parts = pointStr.split(",");
            if (parts.length == 3) {
                try {
                    double x = Double.parseDouble(parts[0].trim());
                    double y = Double.parseDouble(parts[1].trim());
                    double z = Double.parseDouble(parts[2].trim());
                    // --- THE FIX: Use the passed-in worldContext ---
                    spawnerPoints.add(new Location(worldContext, x, y, z));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid spawner-point format: '" + pointStr + "' in world " + worldContext.getName());
                }
            }
        }
        return new MobSpawningFlags(spawnCap, mobs, spawnerPoints);
    }

    @Nullable
    private Location parseLocation(World world, String locationString) {
        if (locationString == null || locationString.isEmpty()) return null;
        String[] parts = locationString.split(",");
        if (parts.length < 3) return null;
        try {
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            double z = Double.parseDouble(parts[2].trim());
            float yaw = (parts.length > 3) ? Float.parseFloat(parts[3].trim()) : 0.0f;
            float pitch = (parts.length > 4) ? Float.parseFloat(parts[4].trim()) : 0.0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            plugin.getLogger().warning("Invalid location format: '" + locationString + "'");
            return null;
        }
    }
}