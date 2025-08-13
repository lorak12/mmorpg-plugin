package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DoTManager {

    // A record to hold the details of a single DoT effect
    public record DoTEffect(UUID targetId, UUID sourceId, double damagePerTick, int remainingTicks) {
        public DoTEffect tick() {
            return new DoTEffect(targetId, sourceId, damagePerTick, remainingTicks - 1);
        }
    }

    // A thread-safe map to store all active DoT effects. Key is the target's UUID.
    private final Map<UUID, DoTEffect> activeEffects = new ConcurrentHashMap<>();

    public DoTManager(MMORPGCore plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                tickEffects();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Ticks once every second (20 ticks)
    }

    public void applyEffect(LivingEntity target, Player source, double damagePerTick, int durationSeconds) {
        int durationTicks = durationSeconds * 20;
        activeEffects.put(target.getUniqueId(), new DoTEffect(target.getUniqueId(), source.getUniqueId(), damagePerTick, durationTicks / 20));
    }

    private void tickEffects() {
        if (activeEffects.isEmpty()) return;

        activeEffects.forEach((uuid, effect) -> {
            LivingEntity target = (LivingEntity) Bukkit.getEntity(uuid);

            if (target == null || target.isDead() || effect.remainingTicks() <= 0) {
                // Remove invalid, dead, or expired effects
                activeEffects.remove(uuid);
                return;
            }

            // Apply damage
            target.damage(effect.damagePerTick(), Bukkit.getPlayer(effect.sourceId()));

            // Update the effect with one less tick
            activeEffects.put(uuid, effect.tick());
        });
    }
}