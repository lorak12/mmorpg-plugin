package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class ThornsEffect implements EnchantmentEffect {
    @Override
    public void onDamaged(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level) {
        // From docs: 50% chance to activate
        if (Math.random() < 0.50) {
            if (event.getDamager() instanceof LivingEntity attacker) {
                // 'value' is the % of damage to reflect
                double reflectPercent = enchantment.getValue(level) / 100.0;
                attacker.damage(event.getFinalDamage() * reflectPercent);
            }
        }
    }
}