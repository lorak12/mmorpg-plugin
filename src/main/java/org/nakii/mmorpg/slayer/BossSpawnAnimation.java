package org.nakii.mmorpg.slayer;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;

import java.util.Random;

/**
 * Handles the multi-stage animation for a Slayer boss spawning.
 */
public class BossSpawnAnimation extends BukkitRunnable {

    private final MMORPGCore plugin;
    private final Player player;
    private final Location spawnLocation;
    private final String bossId;
    private final ConfigurationSection bossConfig;
    private final World world;

    private final Random random = new Random();

    private int ticksLived = 0;
    private static final int DURATION_TICKS = 80; // Total duration of the animation (4 seconds)
    private static final int PILLAR_HEIGHT = 5;
    private static final int ORBIT_PARTICLES = 30;

    public BossSpawnAnimation(MMORPGCore plugin, Player player, Location spawnLocation, String bossId, ConfigurationSection bossConfig) {
        this.plugin = plugin;
        this.player = player;
        this.spawnLocation = spawnLocation.clone().add(0.5, 0.1, 0.5); // Center the location
        this.bossId = bossId;
        this.bossConfig = bossConfig;
        this.world = spawnLocation.getWorld();
    }

    @Override
    public void run() {
        if (ticksLived > DURATION_TICKS) {
            // --- Final Stage: Spawn the Boss ---
            world.playSound(spawnLocation, Sound.ENTITY_WITHER_SPAWN, 2.0f, 1.0f);
            world.spawnParticle(Particle.EXPLOSION, spawnLocation, 1);

            // Spawn the boss and update the quest object
            ActiveSlayerQuest quest = plugin.getSlayerManager().getActiveSlayerQuest(player);
            if (quest != null) {
                plugin.getSlayerManager().finalizeBossSpawn(player, quest, bossId, bossConfig, spawnLocation);
            }

            this.cancel();
            return;
        }

        // --- Animation Stages ---

        // Stage 1: The Pillar (runs for the whole duration)
        if (ticksLived % 2 == 0) { // Spawn every other tick for performance
            for (int i = 0; i < PILLAR_HEIGHT; i++) {
                world.spawnParticle(Particle.SMOKE, spawnLocation.clone().add(0, i * 0.5, 0), 1, 0, 0, 0, 0);
            }
        }

        // Stage 2: Orbiting Particles (Enchanting Table effect)
        double orbitRadius = 1.0;
        for (int i = 0; i < ORBIT_PARTICLES; i++) {
            double angle = (2 * Math.PI * i) / ORBIT_PARTICLES + (ticksLived * 0.1);
            double x = spawnLocation.getX() + orbitRadius * Math.cos(angle);
            double z = spawnLocation.getZ() + orbitRadius * Math.sin(angle);
            world.spawnParticle(Particle.ENCHANT, x, spawnLocation.getY() + 1.5, z, 1, 0, 0, 0, 0);
        }

        // Stage 3: Particles getting "sucked in"
        if (ticksLived > 20) { // Start sucking in after 1 second
            double suckInRadius = 4.0 * (1 - (double) (ticksLived - 20) / (DURATION_TICKS - 20));
            for (int i = 0; i < 10; i++) {
                double angle = randomAngle();
                double x = spawnLocation.getX() + suckInRadius * Math.cos(angle);
                double z = spawnLocation.getZ() + suckInRadius * Math.sin(angle);
                // The "velocity" of the particle is directed towards the pillar's base
                Location particleLoc = new Location(world, x, spawnLocation.getY() + (random.nextDouble() * 3), z);
                world.spawnParticle(Particle.PORTAL, particleLoc, 0,
                        (spawnLocation.getX() - x) * 0.2,
                        (spawnLocation.getY() - particleLoc.getY()) * 0.2,
                        (spawnLocation.getZ() - z) * 0.2,
                        1.0);
            }
        }

        // Sound Effects
        if (ticksLived == 0) {
            world.playSound(spawnLocation, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 0.5f);
        }
        if (ticksLived % 10 == 0) {
            world.playSound(spawnLocation, Sound.BLOCK_CONDUIT_AMBIENT_SHORT, 1.0f, 1.0f + (ticksLived / (float)DURATION_TICKS));
        }

        ticksLived++;
    }

    private double randomAngle() {
        return Math.random() * 2 * Math.PI;
    }
}