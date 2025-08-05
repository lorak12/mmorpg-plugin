package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.entity.CustomZone;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ZoneManager {

    private final MMORPGCore plugin;
    private final Map<String, CustomZone> zoneRegistry = new HashMap<>();
    private final Map<UUID, Location> playerPos1 = new HashMap<>();
    private final Map<UUID, Location> playerPos2 = new HashMap<>();

    public ZoneManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadZones();
        startSpawningTask();
    }

    public void loadZones() {
        zoneRegistry.clear();
        File zonesFile = getZonesFile();
        FileConfiguration zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);

        ConfigurationSection zonesSection = zonesConfig.getConfigurationSection("zones");
        if (zonesSection == null) return;

        for (String key : zonesSection.getKeys(false)) {
            ConfigurationSection section = zonesSection.getConfigurationSection(key);
            if (section != null) {
                World world = Bukkit.getWorld(section.getString("world", ""));
                if (world != null) {
                    zoneRegistry.put(key.toLowerCase(), new CustomZone(key, world, section));
                } else {
                    plugin.getLogger().warning("Could not load zone '" + key + "' because world '" + section.getString("world") + "' does not exist.");
                }
            }
        }
        plugin.getLogger().info("Loaded " + zoneRegistry.size() + " custom zones.");
    }

    public void saveZones() {
        File zonesFile = getZonesFile();
        FileConfiguration zonesConfig = new YamlConfiguration();
        for (CustomZone zone : zoneRegistry.values()) {
            zonesConfig.set("zones." + zone.getId(), zone.getConfig());
        }
        try {
            zonesConfig.save(zonesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startSpawningTask() {
        long spawnInterval = 100L; // 5 seconds
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                for (CustomZone zone : zoneRegistry.values()) {
                    if (zone.contains(player.getLocation())) {
                        trySpawnMobInZone(zone, player);
                        break; // Player can only be in one zone at a time for spawning
                    }
                }
            }
        }, spawnInterval, spawnInterval);
    }

    private void trySpawnMobInZone(CustomZone zone, Player player) {
        ConfigurationSection mobsSection = zone.getConfig().getConfigurationSection("mobs");
        if (mobsSection == null) return;

        for (String key : mobsSection.getKeys(false)) {
            ConfigurationSection mobInfo = mobsSection.getConfigurationSection(key);
            if (mobInfo == null) continue;

            double spawnChance = mobInfo.getDouble("spawn_chance");
            if (ThreadLocalRandom.current().nextDouble() > spawnChance) {
                continue;
            }

            int maxInZone = mobInfo.getInt("max_in_zone");
            String mobId = mobInfo.getString("id");

            int currentCount = 0;
            for (Entity entity : zone.getWorld().getEntitiesByClass(LivingEntity.class)) {
                if (zone.contains(entity.getLocation())) {
                    String id = entity.getPersistentDataContainer().get(new NamespacedKey(plugin, "mob_id"), PersistentDataType.STRING);
                    if (mobId.equalsIgnoreCase(id)) {
                        currentCount++;
                    }
                }
            }

            if (currentCount < maxInZone) {
                spawnMobNearPlayer(mobId, player);
                return; // Only spawn one mob per cycle to prevent clusters
            }
        }
    }

    private void spawnMobNearPlayer(String mobId, Player player) {
        Location playerLoc = player.getLocation();
        int spawnRadius = 25;
        int x = playerLoc.getBlockX() + ThreadLocalRandom.current().nextInt(-spawnRadius, spawnRadius + 1);
        int z = playerLoc.getBlockZ() + ThreadLocalRandom.current().nextInt(-spawnRadius, spawnRadius + 1);
        int y = playerLoc.getWorld().getHighestBlockYAt(x, z) + 1;
        Location spawnLocation = new Location(player.getWorld(), x, y, z);

        plugin.getMobManager().spawnMob(mobId, spawnLocation);
    }

    public void setPlayerSelection(UUID uuid, int pos, Location loc) {
        if (pos == 1) playerPos1.put(uuid, loc);
        else playerPos2.put(uuid, loc);
    }

    public Location getPlayerPos1(UUID uuid) { return playerPos1.get(uuid); }
    public Location getPlayerPos2(UUID uuid) { return playerPos2.get(uuid); }
    public Map<String, CustomZone> getZoneRegistry() { return zoneRegistry; }
    public File getZonesFile() { return new File(plugin.getDataFolder(), "zones.yml"); }
}