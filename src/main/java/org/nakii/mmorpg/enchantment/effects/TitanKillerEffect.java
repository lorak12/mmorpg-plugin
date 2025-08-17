package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.mob.MobStats;

public class TitanKillerEffect implements EnchantmentEffect {
    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getEntity() instanceof LivingEntity target)) return initialDamage;

        MobStats targetStats = MobStats.fromEntity(target);

        // 'value' is the % damage bonus per 100 defense.
        double bonusPer100Def = enchantment.getValue(level) / 100.0;
        // 'cost' is the damage cap in %.
        double cap = enchantment.getCost(level) / 100.0;

        double bonusDamageMultiplier = Math.min(cap, (targetStats.defense() / 100.0) * bonusPer100Def);

        return initialDamage * (1.0 + bonusDamageMultiplier);
    }
}