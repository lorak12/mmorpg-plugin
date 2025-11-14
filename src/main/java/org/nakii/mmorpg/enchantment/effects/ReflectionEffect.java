package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.StatsManager;

public class ReflectionEffect implements EnchantmentEffect {
    private final StatsManager statsManager;

    public ReflectionEffect(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public void onDamaged(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        double intelligence = statsManager.getStats(victim).getIntelligence();
        double damageMultiplier = enchantment.getValue(level);
        double reflectionDamage = intelligence * damageMultiplier;

        attacker.damage(reflectionDamage, victim);
    }
}