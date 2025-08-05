package org.nakii.mmorpg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class MagicListener implements Listener {
    private final MMORPGCore plugin;

    public MagicListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        if (event.isCancelled()) return;

        double multiplier = plugin.getSkillManager().getSkillConfig()
                .getDouble("skills.magic.xp_sources.LEVEL_MULTIPLIER", 0.0);
        double xpToGive = event.getExpLevelCost() * multiplier;

        if (xpToGive > 0) {
            plugin.getSkillManager().addExperience(event.getEnchanter(), Skill.MAGIC, xpToGive);
        }
    }
}