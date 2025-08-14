package org.nakii.mmorpg.enchantment.effects;

import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;

public class RejuvenateEffect implements EnchantmentEffect {
    @Override
    public void onStatRecalculate(PlayerStats stats, CustomEnchantment enchantment, int level) {
        // 'value' is the amount of Health Regen to add
        double healthRegenBonus = enchantment.getValue(level);
        stats.addStat(Stat.HEALTH_REGEN, healthRegenBonus);
    }
}