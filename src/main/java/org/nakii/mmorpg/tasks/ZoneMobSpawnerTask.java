package org.nakii.mmorpg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.managers.MobManager;
import org.nakii.mmorpg.managers.ZoneManager;
import org.nakii.mmorpg.zone.MobSpawningFlags;
import org.nakii.mmorpg.zone.Zone;

import java.util.Random;
import java.util.Set;

public class ZoneMobSpawnerTask extends BukkitRunnable {

    private final ZoneManager zoneManager;
    private final MobManager mobManager;
    private final Random random = new Random();

    // A check to ensure we don't try to spawn mobs in zones with no players nearby.
    private static final double PLAYER_ACTIVATION_RANGE = 64.0;

    public ZoneMobSpawnerTask(MMORPGCore plugin) {
        this.zoneManager = plugin.getZoneManager();
        this.mobManager = plugin.getMobManager();
    }

    @Override
    public void run() {
        Set<Zone> activeZones = zoneManager.getAllPlayerZones();
        if (activeZones.isEmpty()) return;


        for (Zone zone : activeZones) {
            MobSpawningFlags mobFlags = zone.getEffectiveFlags().mobSpawningFlags();
            if (mobFlags == null || mobFlags.spawnCap() <= 0) continue;

            if (isZoneActive(zone)) {
                int currentMobCount = 0;
                // CORRECTED: Pass the world to the helper method
                for (Entity entity : zone.getBounds().getNearbyEntities()) {
                    // CORRECTED: Check if the entity is a LivingEntity before casting/checking
                    if (entity instanceof LivingEntity livingEntity && mobManager.isCustomMob(livingEntity)) {
                        // Check if this mob is actually inside the zone's polygon
                        if (zone.getBounds().contains(livingEntity.getLocation())) {
                            currentMobCount++;
                        }
                    }
                }

                if (currentMobCount < mobFlags.spawnCap()) {
                    spawnMobInZone(zone, mobFlags);
                }
            }
        }
    }

    private void spawnMobInZone(Zone zone, MobSpawningFlags mobFlags) {
        String mobId = mobFlags.getRandomMob(random);
        if (mobId == null) return; // No mobs defined or weight issue

        Location spawnLocation;
        if (mobFlags.spawnerPoints() != null && !mobFlags.spawnerPoints().isEmpty()) {
            // Pick a random predefined spawner point
            spawnLocation = mobFlags.spawnerPoints().get(random.nextInt(mobFlags.spawnerPoints().size()));
        } else {
            // Find a random safe location within the zone's bounds
            spawnLocation = zone.getBounds().getRandomSafeLocation(random);
        }

        if (spawnLocation != null) {
            CustomMobTemplate mobToSpawn = mobManager.getMobById(mobId);
            if(mobToSpawn != null) {
                // Spawn the mob on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        mobManager.spawnMob(mobToSpawn.getId(), spawnLocation, null);
                    }
                }.runTask(MMORPGCore.getInstance());
            }
        }
    }

    private boolean isZoneActive(Zone zone) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (zone.getBounds().contains(player.getLocation())) {
                return true; // A player is inside the zone, it's active
            }
        }
        return false;
    }
}