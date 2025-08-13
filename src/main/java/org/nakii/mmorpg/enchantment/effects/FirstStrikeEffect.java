package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.CombatTracker;

public class FirstStrikeEffect implements EnchantmentEffect {

    private final CombatTracker combatTracker;

    public FirstStrikeEffect() {
        this.combatTracker = MMORPGCore.getInstance().getCombatTracker();
    }

    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player)) {
            return initialDamage;
        }

        CombatTracker.HitData lastHit = combatTracker.getHitData(player);

        // A "first hit" occurs if:
        // 1. There is no previous hit data (the combo expired or never started).
        // 2. The last hit was on a *different* entity.
        boolean isFirstHit = (lastHit == null || !lastHit.targetId().equals(event.getEntity().getUniqueId()));

        if (isFirstHit) {
            double multiplier = 1.0 + (enchantment.getValue(level) / 100.0); // value 25 -> 1.25 multiplier
            return initialDamage * multiplier;
        }

        return initialDamage; // Not the first hit, no damage change
    }
}