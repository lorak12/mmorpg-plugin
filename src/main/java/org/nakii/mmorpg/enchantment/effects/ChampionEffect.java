package org.nakii.mmorpg.enchantment.effects;

import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.CombatTracker;
import org.nakii.mmorpg.utils.ChatUtils;

public class ChampionEffect implements EnchantmentEffect {
    private final CombatTracker combatTracker;

    public ChampionEffect() {
        this.combatTracker = MMORPGCore.getInstance().getCombatTracker();
    }

    @Override
    public void onAttack(EntityDamageByEntityEvent event, CustomEnchantment enchantment, int level, boolean isCritical) {
        if (!(event.getDamager() instanceof Player player)) return;

        CombatTracker.HitData hitData = combatTracker.getHitData(player);
        if (hitData == null || !hitData.targetId().equals(event.getEntity().getUniqueId())) return;

        // Check if this is the second hit
        if (hitData.hitCount() == 2) {
            // 'value' is the extra Combat XP, 'cost' is the coin multiplier
            // For simplicity, we'll just spawn orbs and message about coins.
            int orbValue = (int) (7 + (level -1) * 2);
            double coinValue = 1.4 + (level -1) * 0.4;

            ExperienceOrb orb = player.getWorld().spawn(event.getEntity().getLocation(), ExperienceOrb.class);
            orb.setExperience(orbValue);

            player.sendMessage(ChatUtils.format("<gold>+ " + String.format("%.1f", coinValue) + " coins (Champion)</gold>"));
            // In a real economy, you would call: plugin.getEconomyManager().addCoins(player, coinValue);
        }
    }
}