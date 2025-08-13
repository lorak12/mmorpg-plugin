package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.DoTManager;

public class FireAspectEffect implements EnchantmentEffect {

    private final DoTManager dotManager;

    public FireAspectEffect() {
        this.dotManager = MMORPGCore.getInstance().getDoTManager();
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player) || !(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        // 'value' is the duration in seconds
        int duration = enchantment.getValue(level);
        // 'cost' is the % of weapon damage to deal per tick
        double damagePercent = enchantment.getCost(level) / 100.0;

        double damagePerTick = event.getFinalDamage() * damagePercent;

        dotManager.applyEffect(target, player, damagePerTick, duration);
    }
}