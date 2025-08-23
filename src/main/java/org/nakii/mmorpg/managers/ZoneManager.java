package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.zone.*;

import javax.annotation.Nullable;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class ZoneManager {

    private final MMORPGCore plugin;
    private final Map<String, Zone> loadedZones = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ZoneManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void loadZones() {
        loadedZones.clear();
        File zonesDir = new File(plugin.getDataFolder(), "zones");
        if (!zonesDir.exists()) {
            zonesDir.mkdirs();
        }

        File[] zoneFiles = zonesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (zoneFiles == null) {
            plugin.getLogger().warning("Could not list zone files. The 'zones' directory might be invalid.");
            return;
        }

        for (File zoneFile : zoneFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(zoneFile);
                // **CHANGE**: We now get the ID for the main zone here and pass it to the parser.
                String mainZoneId = config.getString("id");
                if (mainZoneId == null || mainZoneId.isEmpty()) {
                    plugin.getLogger().severe("Main zone file " + zoneFile.getName() + " is missing required 'id' field.");
                    continue;
                }
                Zone zone = parseZone(mainZoneId, config, null);
                if (zone != null) {
                    loadedZones.put(zone.getId(), zone);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load zone from file: " + zoneFile.getName(), e);
            }
        }
        plugin.getLogger().info("Successfully loaded " + loadedZones.size() + " main zones.");
    }

    @Nullable
    public Zone getZoneForLocation(Location location) {
        for (Zone zone : loadedZones.values()) {
            if (zone.getBounds() != null && zone.getBounds().contains(location)) {
                Zone specificZone = zone.getZoneForLocation(location);
                if (specificZone != null) {
                    return specificZone;
                }
            }
        }
        return null;
    }

    // --- YAML Parsing Logic ---

    private Zone parseZone(String id, ConfigurationSection config, @Nullable Zone parent) {
        // --- START OF DEBUG LOG ---
        String parentId = (parent != null) ? parent.getId() : "null";
        // --- END OF DEBUG LOG ---

        var displayName = miniMessage.deserialize(config.getString("display-name", "<red>Unnamed Zone"));
        String icon = config.getString("icon", "❓");
        ZoneBounds bounds = parseBounds(config.getConfigurationSection("bounds"));

        // --- MODIFIED SECTION ---
        // Pass the config section for 'flags' directly to the parser
        ConfigurationSection flagsSection = config.getConfigurationSection("flags");
        ZoneFlags flags = parseFlags(flagsSection); // We pass the section, which can be null
        // --- END OF MODIFIED SECTION ---


        Map<String, SubZone> subZones = new HashMap<>();
        Zone currentZoneForParentage = (parent == null)
                ? new Zone(id, displayName, icon, bounds, flags, Collections.emptyMap())
                : new SubZone(id, displayName, icon, bounds, flags, Collections.emptyMap(), parent);

        ConfigurationSection subZonesSection = config.getConfigurationSection("sub-zones");
        if (subZonesSection != null) {
            for (String subZoneId : subZonesSection.getKeys(false)) {
                ConfigurationSection subZoneConfig = subZonesSection.getConfigurationSection(subZoneId);
                if (subZoneConfig != null) {
                    SubZone subZone = (SubZone) parseZone(subZoneId, subZoneConfig, currentZoneForParentage);
                    if (subZone != null) {
                        subZones.put(subZone.getId(), subZone);
                    }
                }
            }
        }

        if (parent == null) {
            return new Zone(id, displayName, icon, bounds, flags, subZones);
        } else {
            return new SubZone(id, displayName, icon, bounds, flags, subZones, parent);
        }
    }



    private ZoneBounds parseBounds(ConfigurationSection config) {
        if (config == null) return new ZoneBounds(Collections.emptyList(), -64, 320); // Return a default full-world height if no bounds
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
                    plugin.getLogger().warning("Invalid point format in bounds: '" + pointStr + "' in section " + config.getCurrentPath());
                }
            }
        }
        return new ZoneBounds(points, minY, maxY);
    }

    private ZoneFlags parseFlags(ConfigurationSection config) {
        // --- START OF DEBUG LOG ---
        if (config == null) {
            // This is expected if a zone has no 'flags' section at all.
            return ZoneFlags.EMPTY;
        }
        // --- END OF DEBUG LOG ---

        List<String> entryReqs = config.getStringList("entry-requirements");
        Climate climate = null;

        // --- MODIFIED SECTION WITH MORE DEBUGGING ---
        if (config.isConfigurationSection("climate")) {
            ConfigurationSection climateConfig = config.getConfigurationSection("climate");
            climate = new Climate(
                    climateConfig.getString("type", "NEUTRAL"),
                    climateConfig.getInt("required-resistance", 0)
            );
        } else {
        }
        // --- END OF MODIFIED SECTION ---

        BlockBreakingFlags blockBreakingFlags = parseBlockBreakingFlags(config.getConfigurationSection("block-breaking"));
        // ADD THIS LINE
        MobSpawningFlags mobSpawningFlags = parseMobSpawningFlags(config.getConfigurationSection("mob-spawning"));

        return new ZoneFlags(entryReqs, climate, Collections.emptyMap(), null, blockBreakingFlags, mobSpawningFlags); // UPDATE RETURN
    }

    private BlockBreakingFlags parseBlockBreakingFlags(ConfigurationSection config) {
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
                            // Remove 's' from time string and parse
                            revertTime = Long.parseLong(parts[1].replace("s", ""));
                        }

                        String customDropId = nodeConfig.getString("custom-drop-id");
                        String collectionId = nodeConfig.getString("collection-id");
                        int breakingPower = nodeConfig.getInt("breaking-power-required", 0);
                        double breakTime = nodeConfig.getDouble("base-break-time-seconds", 0.0);

                        double skillXp = nodeConfig.getDouble("skill-xp-reward", 0.0);
                        String skillType = nodeConfig.getString("skill-type"); // <-- READ THE NEW KEY

                        definitions.put(nodeId, new BlockNode(nodeId, material, breaksTo, revertsTo, revertTime,
                                customDropId, collectionId, breakingPower, breakTime, skillXp, skillType)); // Update constructor call


                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material in block-breaking definition for node '" + nodeId + "'");
                    }
                }
            }
        }
        return new BlockBreakingFlags(unlistedUnbreakable, definitions);
    }

    private MobSpawningFlags parseMobSpawningFlags(ConfigurationSection config) {
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
        // We assume the world name from the main zone file for now.
        // A more complex system might specify worlds per point.
        String worldName = "world"; // You might need to make this configurable
        for (String pointStr : pointsList) {
            String[] parts = pointStr.split(",");
            if (parts.length == 3) {
                try {
                    double x = Double.parseDouble(parts[0].trim());
                    double y = Double.parseDouble(parts[1].trim());
                    double z = Double.parseDouble(parts[2].trim());
                    spawnerPoints.add(new Location(Bukkit.getWorld(worldName), x, y, z));
                } catch (NumberFormatException | NullPointerException e) {
                    plugin.getLogger().warning("Invalid spawner-point format: '" + pointStr + "'");
                }
            }
        }
        return new MobSpawningFlags(spawnCap, mobs, spawnerPoints);
    }

    public Set<Zone> getAllPlayerZones() {
        Set<Zone> activeZones = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Zone zone = getZoneForLocation(player.getLocation());
            if (zone != null) {
                activeZones.add(zone);
            }
        }
        return activeZones;
    }

    /**
     * Gets a set of all loaded main zone IDs.
     * @return A set of zone IDs.
     */
    public Set<String> getZoneIds() {
        return loadedZones.keySet();
    }

    /**
     * Gets a set of all sub-zone IDs for a given parent zone.
     * @param parentZoneId The ID of the main zone.
     * @return A set of sub-zone IDs, or an empty set if the parent doesn't exist or has no sub-zones.
     */
    public Set<String> getSubZoneIds(String parentZoneId) {
        Zone parent = loadedZones.get(parentZoneId.toLowerCase());
        if (parent != null && parent.getSubZones() != null) {
            return parent.getSubZones().keySet();
        }
        return Collections.emptySet();
    }
}