package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class GiantKillerEffect implements EnchantmentEffect {

    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player) || !(event.getEntity() instanceof LivingEntity target)) {
            return initialDamage;
        }

        double playerHealth = player.getHealth();
        double targetHealth = target.getHealth();

        if (targetHealth <= playerHealth) {
            return initialDamage; // Target doesn't have more health, no bonus.
        }

        double healthDifferencePercent = ((targetHealth - playerHealth) / playerHealth) * 100.0;

        // The 'value' from YAML is the damage increase per 1% of extra health.
        // E.g., value=0.1 means +0.1% damage for every 1% more health the target has.
        double damageIncreasePerPercent = enchantment.getValue(level) / 100.0; // Convert 0.1 to 0.001

        // The 'cap' is stored as the cost in the YAML for simplicity.
        double cap = enchantment.getCost(level) / 100.0; // Convert 5 to 0.05 for 5% cap

        double bonusDamageMultiplier = Math.min(cap, healthDifferencePercent * damageIncreasePerPercent);

        return initialDamage * (1.0 + bonusDamageMultiplier);
    }
}