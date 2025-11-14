package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.DebuffManager;

public class LethalityEffect implements EnchantmentEffect {

    private static final int MAX_STACKS = 4;
    private final DebuffManager debuffManager;

    public LethalityEffect(DebuffManager debuffManager) {
        this.debuffManager = debuffManager;
    }

    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getEntity() instanceof LivingEntity target)) return initialDamage;

        int stacks = debuffManager.getLethalityStacks(target.getUniqueId());
        if (stacks > 0) {
            double reductionPerStack = enchantment.getValue(level) / 100.0;
            double totalReduction = stacks * reductionPerStack;
            return initialDamage * (1.0 + totalReduction);
        }
        return initialDamage;
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int currentStacks = debuffManager.getLethalityStacks(target.getUniqueId());
        int newStacks = Math.min(MAX_STACKS, currentStacks + 1);
        debuffManager.applyLethalityStack(target.getUniqueId(), newStacks);
    }
}