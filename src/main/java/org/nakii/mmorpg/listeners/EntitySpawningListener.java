package org.nakii.mmorpg.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import java.util.concurrent.ThreadLocalRandom;

public class EntitySpawningListener implements Listener {
    private final MMORPGCore plugin;

    public EntitySpawningListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled() || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return; // Ignore our own custom-spawned mobs
        }

        LivingEntity entity = event.getEntity();

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            // Step 1: Generate Data
            int level = ThreadLocalRandom.current().nextInt(1, 11); // Level 1-10
            double baseHealth = entity.getHealth();
            double finalHealth = baseHealth * (1 + (level - 1) * 0.2); // +20% health per level

            // Step 2: Store the level directly on the entity's metadata
            entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "mob_level"), PersistentDataType.INTEGER, level);

            // Step 3: Register the entity with our health system
            plugin.getHealthManager().registerEntity(entity, finalHealth);

            // Step 4: Schedule the name update using the centralized method
            // The 1-tick delay ensures the entity is fully initialized in the world.
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (entity.isValid()) {
                    plugin.getDamageManager().updateMobHealthDisplay(entity);
                }
            }, 1L);

        } else {
            // For mobs from spawners, eggs, etc., just register them with base health.
            plugin.getHealthManager().registerEntity(entity);
        }
    }
}