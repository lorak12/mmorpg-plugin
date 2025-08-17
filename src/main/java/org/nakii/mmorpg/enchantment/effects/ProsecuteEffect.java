package org.nakii.mmorpg.enchantment.effects;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.mob.MobStats;

public class ProsecuteEffect implements EnchantmentEffect {
    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getEntity() instanceof LivingEntity target)) return initialDamage;
        MobStats targetStats = MobStats.fromEntity(target);
        double currentHealthPercent = targetStats.health() / targetStats.maxHealth();

        // 'value' from YAML is the % damage increase per 1% of current health.
        double damageIncreasePerPercent = enchantment.getValue(level) / 100.0;

        double totalDamageMultiplier = 1.0 + (currentHealthPercent * 100.0 * damageIncreasePerPercent);

        return initialDamage * totalDamageMultiplier;
    }
}