package org.nakii.mmorpg.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.nakii.mmorpg.MMORPGCore;

public class EntitySpawningListener implements Listener {

    private final MMORPGCore plugin;

    public EntitySpawningListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        plugin.getHealthManager().registerEntity(entity);
        plugin.getDamageManager().updateMobHealthDisplay(entity);
    }
}
