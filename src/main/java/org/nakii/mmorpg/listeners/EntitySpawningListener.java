package org.nakii.mmorpg.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.MobManager;

public class EntitySpawningListener implements Listener {

    private final MMORPGCore plugin;
    private final MobManager mobManager;

    public EntitySpawningListener(MMORPGCore plugin, MobManager mobManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (mobManager.isCustomMob(entity)) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    mobManager.updateHealthDisplay(entity);
                });
            }
        }
    }
}