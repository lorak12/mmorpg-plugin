package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.CombatTracker;
import org.nakii.mmorpg.player.Stat;

public class CounterStrikeEffect implements EnchantmentEffect {

    private final CombatTracker combatTracker;

    public CounterStrikeEffect() {
        this.combatTracker = MMORPGCore.getInstance().getCombatTracker();
    }

    @Override
    public void onDamaged(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level) {
        if (!(event.getEntity() instanceof Player victim)) return;

        // Use the combat tracker to see if this is the first hit from this attacker
        CombatTracker.HitData hitData = combatTracker.getHitData(victim); // Note: we're tracking who hit the victim
        boolean isFirstHit = (hitData == null || !hitData.targetId().equals(event.getDamager().getUniqueId()));

        if (isFirstHit) {
            // 'value' is the defense bonus
            double defenseBonus = enchantment.getValue(level);
            MMORPGCore.getInstance().getTimedBuffManager().applyBuff(
                    victim,
                    Stat.DEFENSE,
                    defenseBonus,
                    "counter_strike",
                    7 // 7 second duration
            );
        }
    }
}