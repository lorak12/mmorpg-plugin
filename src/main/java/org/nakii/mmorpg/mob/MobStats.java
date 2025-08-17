package org.nakii.mmorpg.mob;

import org.bukkit.entity.LivingEntity;

// A simple record to hold basic stats for non-player entities.
public record MobStats(double health, double maxHealth, double defense) {
    public static MobStats fromEntity(LivingEntity entity) {
        // For now, mobs have 0 custom defense. This can be expanded later.
        return new MobStats(entity.getHealth(), entity.getMaxHealth(), 0.0);
    }
}