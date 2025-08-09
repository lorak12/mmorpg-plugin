package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.zone.CustomZone;
import org.nakii.mmorpg.zone.MobSpawnInfo;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ZoneMobSpawnerManager {
    private final MMORPGCore plugin;

    public ZoneMobSpawnerManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void startSpawnTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    attemptToSpawnMobNearPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 100L, 100L); // Run every 5 seconds
    }

    private void attemptToSpawnMobNearPlayer(Player player) {
        ZoneManager zoneManager = plugin.getZoneManager();
        CustomZone zone = zoneManager.findZoneAt(player.getLocation());

        if (zone == null || zone.getMobs().isEmpty()) return;

        List<MobSpawnInfo> potentialSpawns = zone.getMobs();
        for (MobSpawnInfo spawnInfo : potentialSpawns) {
            // Check spawn chance
            if (ThreadLocalRandom.current().nextDouble() > spawnInfo.spawnChance) continue;

            // Check mob cap for this specific mob type in this zone (more advanced, for now we do a total cap)
            if (zoneManager.getMobCountInZone(zone.getId()) >= spawnInfo.maxInZone) continue;

            // Find a valid location
            Location spawnLoc = findValidSpawnLocation(player.getLocation());
            if (spawnLoc == null) continue;

            // Spawn the mob
            LivingEntity spawnedMob = plugin.getMobManager().spawnMob(spawnInfo.mobId, spawnLoc);
            if (spawnedMob != null) {
                zoneManager.trackMob(spawnedMob, zone.getId());
                return; // Only spawn one mob per player per cycle
            }
        }
    }

    private Location findValidSpawnLocation(Location center) {
        for (int i = 0; i < 10; i++) { // Try 10 times to find a spot
            int x = center.getBlockX() + ThreadLocalRandom.current().nextInt(-15, 16);
            int z = center.getBlockZ() + ThreadLocalRandom.current().nextInt(-15, 16);
            int y = center.getWorld().getHighestBlockYAt(x, z);

            Location loc = new Location(center.getWorld(), x + 0.5, y + 1, z + 0.5);
            if (loc.getBlock().getType().isAir() && loc.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                return loc;
            }
        }
        return null;
    }
}