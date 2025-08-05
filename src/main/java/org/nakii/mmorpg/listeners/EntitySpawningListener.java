package org.nakii.mmorpg.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.nakii.mmorpg.MMORPGCore;
import java.util.concurrent.ThreadLocalRandom;

public class EntitySpawningListener implements Listener {
    private final MMORPGCore plugin;

    public EntitySpawningListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;

        // Handle custom mobs spawned by our plugin
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            // We assume that CUSTOM spawns are handled by our MobManager and need no further processing here.
            return;
        }

        LivingEntity entity = event.getEntity();
        // Handle naturally spawning mobs
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            int level = ThreadLocalRandom.current().nextInt(1, 11); // Level 1-10
            double baseHealth = entity.getHealth();
            double finalHealth = baseHealth * (1 + (level - 1) * 0.2); // +20% health per level above 1

            plugin.getHealthManager().registerEntity(entity, finalHealth);
            entity.setCustomName(ChatColor.GRAY + "[Lv. " + level + "] " + ChatColor.WHITE + entity.getType().name());
            entity.setCustomNameVisible(true);
        } else {
            // Handle all other mob spawns (eggs, spawners, etc.)
            plugin.getHealthManager().registerEntity(entity);
        }
    }
}