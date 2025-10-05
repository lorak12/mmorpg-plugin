package org.nakii.mmorpg.util;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nakii.mmorpg.MMORPGCore;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages all direct read/write operations for zone configuration files.
 */
public class ZoneConfigManager {

    private final File zonesDir;

    public ZoneConfigManager(MMORPGCore plugin) {
        this.zonesDir = new File(plugin.getDataFolder(), "zones");
        if (!zonesDir.exists()) {
            zonesDir.mkdirs();
        }
    }

    private File getZoneFile(String zoneId) {
        return new File(zonesDir, zoneId.toLowerCase() + ".yml");
    }

    public boolean zoneExists(String zoneId) {
        return getZoneFile(zoneId).exists();
    }

    public boolean createZoneFile(String zoneId, String displayName) {
        File zoneFile = getZoneFile(zoneId);
        if (zoneFile.exists()) {
            return false; // Zone already exists
        }

        try {
            zoneFile.createNewFile();
            YamlConfiguration config = new YamlConfiguration();
            config.set("id", zoneId);
            config.set("display-name", displayName);
            // Set some default empty values for structure
            config.createSection("bounds");
            config.createSection("flags");
            config.createSection("sub-zones");
            config.save(zoneFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteZoneFile(String zoneId) {
        File zoneFile = getZoneFile(zoneId);
        if (!zoneFile.exists()) {
            return false; // Zone doesn't exist
        }
        return zoneFile.delete();
    }

    public boolean setPoints(String zoneId, String subZonePath, List<Location> points) {
        File zoneFile = getZoneFile(zoneId);
        if (!zoneFile.exists()) return false;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(zoneFile);
        String basePath = subZonePath.isEmpty() ? "" : "sub-zones." + subZonePath;

        if (!subZonePath.isEmpty() && !config.isConfigurationSection(basePath)) {
            return false; // Sub-zone path does not exist
        }

        List<String> pointStrings = points.stream()
                .map(loc -> loc.getX() + "," + loc.getZ())
                .collect(Collectors.toList());

        config.set(basePath + ".bounds.points", pointStrings);

        try {
            config.save(zoneFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setHeight(String zoneId, String subZonePath, int minY, int maxY) {
        File zoneFile = getZoneFile(zoneId);
        if (!zoneFile.exists()) return false;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(zoneFile);
        String basePath = subZonePath.isEmpty() ? "" : "sub-zones." + subZonePath;

        if (!subZonePath.isEmpty() && !config.isConfigurationSection(basePath)) {
            return false;
        }

        config.set(basePath + ".bounds.min-y", minY);
        config.set(basePath + ".bounds.max-y", maxY);

        try {
            config.save(zoneFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}