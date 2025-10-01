package org.nakii.mmorpg.tasks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.MobManager;
import org.nakii.mmorpg.managers.WorldManager;
import org.nakii.mmorpg.zone.MobSpawningFlags;
import org.nakii.mmorpg.zone.Zone;
import org.nakii.mmorpg.world.CustomWorld;

import java.util.Random;

public class MobSpawningTask extends BukkitRunnable {

    private final WorldManager worldManager;
    private final MobManager mobManager;
    private final Random random = new Random();

    public MobSpawningTask(MMORPGCore plugin) {
        this.worldManager = plugin.getWorldManager();
        this.mobManager = plugin.getMobManager();
    }

    @Override
    public void run() {
        // --- CORE CHANGE: Iterate through our new CustomWorld objects ---
        for (CustomWorld world : worldManager.getLoadedWorlds()) {
            // First, check if there are any players in this world to justify spawning mobs.
            if (world.getBukkitWorld().getPlayerCount() == 0) {
                continue;
            }

            for (Zone zone : world.getZones()) {
                MobSpawningFlags mobFlags = zone.getFlags().mobSpawningFlags();
                if (mobFlags == null || mobFlags.spawnCap() <= 0) {
                    continue;
                }

                // Check if any players are nearby before spawning.
                if (!isZoneActive(zone, world.getBukkitWorld())) {
                    continue;
                }

                // Count existing custom mobs in the zone.
                long currentMobCount = zone.getBounds().getNearbyEntities(world.getBukkitWorld())
                        .stream()
                        .filter(entity -> entity instanceof LivingEntity && mobManager.isCustomMob((LivingEntity) entity))
                        .filter(entity -> zone.getBounds().contains(entity.getLocation())) // More precise check
                        .count();

                if (currentMobCount < mobFlags.spawnCap()) {
                    spawnMobInZone(zone, mobFlags, world.getBukkitWorld());
                }
            }
        }
    }

    private void spawnMobInZone(Zone zone, MobSpawningFlags mobFlags, World world) {
        String mobId = mobFlags.getRandomMob(random);
        if (mobId == null) return;

        Location spawnLocation;
        if (mobFlags.spawnerPoints() != null && !mobFlags.spawnerPoints().isEmpty()) {
            spawnLocation = mobFlags.spawnerPoints().get(random.nextInt(mobFlags.spawnerPoints().size()));
        } else {
            // Pass the world context to the safe location finder.
            spawnLocation = zone.getBounds().getRandomSafeLocation(random, world);
        }

        if (spawnLocation != null) {
            // The MobManager handles spawning on the main thread if needed, but we ensure it.
            new BukkitRunnable() {
                @Override
                public void run() {
                    mobManager.spawnMob(mobId, spawnLocation, null);
                }
            }.runTask(MMORPGCore.getInstance());
        }
    }

    private boolean isZoneActive(Zone zone, World world) {
        for (Player player : world.getPlayers()) {
            if (zone.getBounds().contains(player.getLocation())) {
                return true; // A player is inside the zone, it's active.
            }
        }
        return false;
    }
}