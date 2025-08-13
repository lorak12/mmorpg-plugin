package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class ExecuteEffect implements EnchantmentEffect {

    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getEntity() instanceof LivingEntity target)) return initialDamage;

        double maxHealth = target.getMaxHealth();
        double currentHealth = target.getHealth();
        double missingHealthPercent = (1.0 - (currentHealth / maxHealth)) * 100.0;

        // The 'value' from YAML is the damage increase per 1% of missing health.
        // E.g., value=0.2 means +0.2% damage for every 1% health missing.
        double damageIncreasePerPercent = enchantment.getValue(level) / 100.0; // Convert 0.2 to 0.002

        double totalDamageMultiplier = 1.0 + (missingHealthPercent * damageIncreasePerPercent);

        return initialDamage * totalDamageMultiplier;
    }
}