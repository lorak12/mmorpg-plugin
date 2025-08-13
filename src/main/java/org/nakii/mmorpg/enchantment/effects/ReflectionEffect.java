package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class ReflectionEffect implements EnchantmentEffect {

    @Override
    public void onDamaged(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        // Get the victim's Intelligence stat
        double intelligence = MMORPGCore.getInstance().getStatsManager().getStats(victim).getIntelligence();

        // 'value' is the damage multiplier from YAML
        double damageMultiplier = enchantment.getValue(level);
        double reflectionDamage = intelligence * damageMultiplier;

        // Damage the attacker, sourcing it from the victim
        attacker.damage(reflectionDamage, victim);
    }
}