package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class KnockbackEffect implements EnchantmentEffect {

    @Override
    public void onAttack(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player)) return;

        // The 'value' from YAML is the knockback strength multiplier.
        double strength = enchantment.getValue(level);

        // Get the direction from the player to the target
        Vector direction = event.getEntity().getLocation().toVector().subtract(player.getLocation().toVector()).normalize();

        // Apply a velocity change. We add a slight upward push for a more vanilla feel.
        direction.multiply(strength * 0.4); // Adjust this multiplier for desired strength
        direction.setY(0.35);

        event.getEntity().setVelocity(direction);
    }
}