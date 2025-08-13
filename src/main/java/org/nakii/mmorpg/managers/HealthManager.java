package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerStats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HealthManager {

    private final MMORPGCore plugin;
    private final Map<UUID, Double> currentHealthMap = new HashMap<>();

    public HealthManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void setEntityHealth(LivingEntity entity, double maxHealth) {
        currentHealthMap.put(entity.getUniqueId(), maxHealth);
    }

    public void unregisterEntity(LivingEntity entity) {
        currentHealthMap.remove(entity.getUniqueId());
    }

    private void ensureTracked(LivingEntity entity) {
        currentHealthMap.computeIfAbsent(entity.getUniqueId(), k -> {
            if (plugin.getMobManager().isCustomMob(entity)) {
                return plugin.getMobManager().getCustomMob(plugin.getMobManager().getMobId(entity)).getStatsConfig().getDouble("stats.health", 20.0);
            }
            return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null ? entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 20.0;
        });
    }

    public double getMaxHealth(LivingEntity entity) {
        if (entity instanceof org.bukkit.entity.Player) {
            return plugin.getStatsManager().getStats((org.bukkit.entity.Player) entity).getHealth();
        }
        ensureTracked(entity);
        return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    public double getCurrentHealth(LivingEntity entity) {
        ensureTracked(entity);
        return currentHealthMap.get(entity.getUniqueId());
    }

    public void setCurrentHealth(LivingEntity entity, double health) {
        ensureTracked(entity);
        double maxHealth = getMaxHealth(entity);
        double newHealth = Math.max(0, Math.min(maxHealth, health));
        currentHealthMap.put(entity.getUniqueId(), newHealth);

        // If health drops to 0, kill the entity. This is the ONLY place
        // we should ever directly affect vanilla health.
        if (newHealth <= 0 && !entity.isDead()) {
            entity.setHealth(0);
        }
    }

    public void applyDamage(LivingEntity entity, double damage) {
        if (damage <= 0) return;
        double currentHealth = getCurrentHealth(entity);
        setCurrentHealth(entity, currentHealth - damage);
    }

    public boolean isManagedEntity(LivingEntity entity) {
        return currentHealthMap.containsKey(entity.getUniqueId());
    }

    /**
     * Starts the repeating task that handles health regeneration for all online players.
     */
    public void startHealthRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isDead()) continue;

                    double currentHealth = getCurrentHealth(player);
                    double maxHealth = getMaxHealth(player);

                    if (currentHealth >= maxHealth) continue;

                    PlayerStats stats = plugin.getStatsManager().getStats(player);
                    double healthRegenStat = stats.getHealthRegen();

                    double baseRegen = maxHealth * 0.02; // Base regen is 2% of max health
                    double finalRegen = baseRegen * (healthRegenStat / 100.0);

                    if (finalRegen > 0) {
                        heal(player, finalRegen);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Regen every second
    }

    // ... your other methods like setEntityHealth, unregisterEntity, etc. remain here ...

    public void heal(LivingEntity entity, double amount) {
        if (amount <= 0) return;
        double currentHealth = getCurrentHealth(entity);
        setCurrentHealth(entity, currentHealth + amount);
    }
}