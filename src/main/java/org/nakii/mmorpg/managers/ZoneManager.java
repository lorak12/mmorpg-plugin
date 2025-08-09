package org.nakii.mmorpg.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.zone.CustomSubZone;
import org.nakii.mmorpg.zone.CustomZone;
import org.nakii.mmorpg.zone.EnvironmentSettings;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneManager {
    private final MMORPGCore plugin;
    private final Map<String, CustomZone> zoneRegistry = new HashMap<>();
    private final Map<String, Integer> mobCount = new ConcurrentHashMap<>();
    private final Map<UUID, String> entityToZoneMap = new ConcurrentHashMap<>();

    public ZoneManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadZones();
    }

    public void loadZones() {
        zoneRegistry.clear();
        File zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        if (!zonesFile.exists()) {
            plugin.saveResource("zones.yml", false);
        }
        FileConfiguration zoneConfig = YamlConfiguration.loadConfiguration(zonesFile);

        if (!zoneConfig.isConfigurationSection("zones")) {
            plugin.getLogger().info("No 'zones' section found in zones.yml. Skipping zone loading.");
            return; // Exit the method early if there's nothing to load.
        }

        for (String key : zoneConfig.getConfigurationSection("zones").getKeys(false)) {
            zoneRegistry.put(key, new CustomZone(key, zoneConfig.getConfigurationSection("zones." + key)));
        }
        plugin.getLogger().info("Loaded " + zoneRegistry.size() + " top-level zones.");
    }

    public CustomZone findZoneAt(Location location) {
        for (CustomZone zone : zoneRegistry.values()) {
            if (zone.contains(location)) {
                // Prioritize sub-zones
                for (CustomSubZone subZone : zone.getSubZones().values()) {
                    if (subZone.contains(location)) {
                        return subZone;
                    }
                }
                return zone; // Return parent zone if no sub-zone matches
            }
        }
        return null; // No zone found
    }

    // These methods now work correctly by using findZoneAt()
    public boolean isSafeZone(Location location) {
        CustomZone zone = findZoneAt(location);
        return zone != null && zone.getMobs().isEmpty();
    }

    public String getZoneEnvironmentType(Location location) {
        CustomZone zone = findZoneAt(location);
        if (zone != null && zone.getEnvironment() != null) {
            return zone.getEnvironment().type;
        }
        return "natural";
    }

    public double getZoneValue(Location location, String path, double defaultValue) {
        CustomZone zone = findZoneAt(location);
        if (zone == null || zone.getEnvironment() == null) return defaultValue;
        EnvironmentSettings env = zone.getEnvironment();
        switch (path) {
            case "increase_per_second": return env.increasePerSecond;
            case "max_value": return env.maxValue;
            case "damage_threshold": return env.damageThreshold;
            case "damage_amount": return env.damageAmount;
            default: return defaultValue;
        }
    }

    // --- Mob Spawning Helpers ---
    public int getMobCountInZone(String zoneId) {
        return mobCount.getOrDefault(zoneId, 0);
    }

    public void trackMob(Entity entity, String zoneId) {
        entityToZoneMap.put(entity.getUniqueId(), zoneId);
        mobCount.put(zoneId, mobCount.getOrDefault(zoneId, 0) + 1);
    }

    public void untrackMob(Entity entity) {
        String zoneId = entityToZoneMap.remove(entity.getUniqueId());
        if (zoneId != null) {
            mobCount.put(zoneId, Math.max(0, mobCount.getOrDefault(zoneId, 1) - 1));
        }
    }
}