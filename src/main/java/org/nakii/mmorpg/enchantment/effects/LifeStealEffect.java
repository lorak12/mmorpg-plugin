package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class LifeStealEffect implements EnchantmentEffect {
    @Override
    public void onAttack(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player)) return;

        double healPercent = enchantment.getValue(level) * 0.005;
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        double healAmount = maxHealth * healPercent;

        player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));
    }
}