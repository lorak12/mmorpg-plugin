package org.nakii.mmorpg.managers;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealthManager {

    private final MMORPGCore plugin;
    private final Map<UUID, Double> currentHealth = new HashMap<>();
    private final Map<UUID, Double> maxHealth = new HashMap<>();

    public HealthManager(MMORPGCore plugin) {
        this.plugin = plugin;
        startHealthRegenTask();
    }

    public void registerEntity(LivingEntity entity) {
        double baseHealth;
        if (entity instanceof Player) {
            baseHealth = plugin.getStatsManager().getStats((Player) entity).getHealth();
        } else {
            // Default health for mobs, can be customized later
            baseHealth = 20.0;
        }
        maxHealth.put(entity.getUniqueId(), baseHealth);
        currentHealth.put(entity.getUniqueId(), baseHealth);

        // Keep vanilla health bar full for visual control
        entity.setHealth(entity.getMaxHealth());
    }

    public void unregisterEntity(LivingEntity entity) {
        currentHealth.remove(entity.getUniqueId());
        maxHealth.remove(entity.getUniqueId());
    }

    public double getCurrentHealth(LivingEntity entity) {
        return currentHealth.getOrDefault(entity.getUniqueId(), 20.0);
    }

    public double getMaxHealth(LivingEntity entity) {
        return maxHealth.getOrDefault(entity.getUniqueId(), 20.0);
    }

    public void setCurrentHealth(LivingEntity entity, double health) {
        double newHealth = Math.max(0, Math.min(getMaxHealth(entity), health));
        currentHealth.put(entity.getUniqueId(), newHealth);
    }

    public void updateMaxHealth(LivingEntity entity) {
        double oldMax = getMaxHealth(entity);
        double newMax;

        if (entity instanceof Player) {
            newMax = plugin.getStatsManager().getStats((Player) entity).getHealth();
        } else {
            newMax = oldMax;
        }

        maxHealth.put(entity.getUniqueId(), newMax);
        double healthPercentage = getCurrentHealth(entity) / oldMax;
        setCurrentHealth(entity, newMax * healthPercentage);
    }

    private void startHealthRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : currentHealth.keySet()) {
                    LivingEntity entity = (LivingEntity) plugin.getServer().getEntity(uuid);
                    if (entity != null && entity.isValid() && entity instanceof Player) {
                        Player player = (Player) entity;
                        double hpRegen = plugin.getStatsManager().getStats(player).getHpRegen();
                        if (hpRegen > 0) {
                            double current = getCurrentHealth(player);
                            double max = getMaxHealth(player);
                            if (current < max) {
                                setCurrentHealth(player, current + hpRegen);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }
}