package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class CleaveEffect implements EnchantmentEffect {
    @Override
    public void onAttack(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player)) return;

        double damagePercent = enchantment.getValue(level) * 0.01; // value 5 = 5%
        double radius = 3 + (0.5 * level);
        double aoeDamage = event.getFinalDamage() * damagePercent;

        for (Entity nearbyEntity : player.getNearbyEntities(radius, radius, radius)) {
            if (nearbyEntity instanceof LivingEntity target && nearbyEntity != event.getEntity()) {
                if (!target.isDead() && target.getType().isAlive()) {
                    target.damage(aoeDamage, player);
                }
            }
        }
    }
}