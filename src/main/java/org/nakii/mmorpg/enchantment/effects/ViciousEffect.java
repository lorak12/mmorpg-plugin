package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.nakii.mmorpg.enchantment.CustomEnchantment;

public class ViciousEffect implements EnchantmentEffect {
    @Override
    public void onKill(EntityDeathEvent event, Player killer, CustomEnchantment enchantment, int level) {
        // We will assume a PotionEffectManager exists to handle this.
        // The 'value' from YAML is the amount of Ferocity to grant.
        int ferocityAmount = enchantment.getValue(level);

        // The 'cost' from YAML is the duration in seconds.
        int duration = enchantment.getCost(level);

        // MMORPGCore.getInstance().getPotionEffectManager().applyFerocity(killer, ferocityAmount, duration);
        // For now, let's just send a message to confirm it works.
        killer.sendMessage("Vicious triggered! +" + ferocityAmount + " Ferocity for " + duration + "s.");
    }
}