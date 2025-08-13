package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.DebuffManager;

public class VenomousEffect implements EnchantmentEffect {

    private static final int MAX_STACKS = 40;
    private static final int DURATION_TICKS = 5 * 20;
    private final DebuffManager debuffManager;

    public VenomousEffect() {
        this.debuffManager = MMORPGCore.getInstance().getDebuffManager();
    }

    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getEntity() instanceof LivingEntity target)) return initialDamage;

        int stacks = debuffManager.getVenomousStacks(target.getUniqueId());
        if (stacks > 0) {
            double damageIncreasePerStack = enchantment.getValue(level) / 100.0;
            double totalDamageMultiplier = 1.0 + (stacks * damageIncreasePerStack);
            return initialDamage * totalDamageMultiplier;
        }
        return initialDamage;
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int slowAmplifier = enchantment.getCost(level) - 1;
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, DURATION_TICKS, slowAmplifier));

        int currentStacks = debuffManager.getVenomousStacks(target.getUniqueId());
        int newStacks = Math.min(MAX_STACKS, currentStacks + 1);
        debuffManager.applyVenomousStack(target.getUniqueId(), newStacks);
    }
}