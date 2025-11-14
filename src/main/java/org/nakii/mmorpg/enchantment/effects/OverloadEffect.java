package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.StatsManager;

public class OverloadEffect implements EnchantmentEffect {

    private final StatsManager statsManager;

    public OverloadEffect(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player)) {
            return initialDamage;
        }

        // Check for the "Mega Critical Hit" condition using the stored manager.
        if (isCritical && statsManager.getStats(player).getCritChance() > 100) {
            // 'cost' is the mega crit bonus % from the YAML
            double megaCritBonus = enchantment.getCost(level) / 100.0;
            return initialDamage * (1.0 + megaCritBonus);
        }

        return initialDamage;
    }
}