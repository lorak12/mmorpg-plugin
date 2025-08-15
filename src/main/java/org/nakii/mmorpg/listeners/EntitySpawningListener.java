package org.nakii.mmorpg.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.nakii.mmorpg.MMORPGCore;


public class EntitySpawningListener implements Listener {

    private final MMORPGCore plugin;

    public EntitySpawningListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            // --- THIS IS THE FIX ---
            // We first check if the spawned entity is one of our custom mobs.
            // If it's not (i.e., it's a regular vanilla mob), we do nothing.
            if (plugin.getMobManager().isCustomMob(entity)) {
                // Because we run this 1 tick later, it can prevent some race conditions
                // and ensures the mob is fully initialized in the world.
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getMobManager().updateHealthDisplay(entity);
                });
            }
        }
    }
}