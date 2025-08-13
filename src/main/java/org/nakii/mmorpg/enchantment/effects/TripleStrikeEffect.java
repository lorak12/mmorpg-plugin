package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.CombatTracker;

public class TripleStrikeEffect implements EnchantmentEffect {

    private final CombatTracker combatTracker;
    private static final int MAX_COMBO_HITS = 3;

    public TripleStrikeEffect() {
        this.combatTracker = MMORPGCore.getInstance().getCombatTracker();
    }

    @Override
    public double onDamageModify(double initialDamage, EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player)) return initialDamage;

        CombatTracker.HitData hitData = combatTracker.getHitData(player);
        int hitCount = (hitData != null && hitData.targetId().equals(event.getEntity().getUniqueId()))
                ? hitData.hitCount() + 1 // We check the *next* hit count
                : 1;

        if (hitCount <= MAX_COMBO_HITS) {
            double multiplier = 1.0 + (enchantment.getValue(level) / 100.0); // value 10 -> 1.10 multiplier
            return initialDamage * multiplier;
        }

        return initialDamage;
    }
}