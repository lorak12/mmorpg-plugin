package org.nakii.mmorpg.listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;


public class EntitySpawningListener implements Listener {

    private final MMORPGCore plugin;

    public EntitySpawningListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {

        if (event.getEntityType() == EntityType.ARMOR_STAND) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getEntity();

        // Don't modify our own custom mobs during their spawn event
        if (plugin.getMobManager().isCustomMob(entity)) {
            return;
        }

        // We schedule the entire logic block to run one server tick later.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            // Ensure the entity hasn't been removed in the tiny interval
            if (!entity.isValid()) {
                return;
            }

            // 1. Give the naturally spawned mob a default level.
            entity.getPersistentDataContainer().set(new NamespacedKey(plugin, "mob_level"), PersistentDataType.INTEGER, 1);

            // 2. Get the mob's default vanilla max health.
            double vanillaMaxHealth = 20.0;
            if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                vanillaMaxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            }

            // 3. Register the vanilla mob into our health system.
            plugin.getHealthManager().setEntityHealth(entity, vanillaMaxHealth);

            // 4. Update its nameplate. By now, the server has finished its own
            // spawning logic, so our name tag change will not be overwritten.
            plugin.getMobManager().updateHealthDisplay(entity);

        }, 1L); // The '1L' means "1 tick from now".
    }
}