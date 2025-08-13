package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class VampirismEffect implements EnchantmentEffect {

    @Override
    public void onKill(EntityDeathEvent event, Player killer, CustomEnchantment enchantment, int level) {
        double maxHealth = killer.getMaxHealth();
        double missingHealth = maxHealth - killer.getHealth();

        // The 'value' from YAML is the percentage of missing health to heal.
        // E.g., value=1 means heal for 1% of missing health.
        double healPercent = enchantment.getValue(level) / 100.0; // Convert 1 to 0.01

        double healAmount = missingHealth * healPercent;

        killer.setHealth(Math.min(maxHealth, killer.getHealth() + healAmount));
    }
}