package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.nakii.mmorpg.managers.SkillManager;
import org.nakii.mmorpg.skills.Skill;

public class CarpentryListener implements Listener {

    private final SkillManager skillManager;

    public CarpentryListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player player)) return;

        double multiplier = skillManager.getSkillsConfig().getDouble("CARPENTRY.xp-sources.CRAFT_MULTIPLIER", 0.1);
        double xpToGive = event.getRecipe().getResult().getAmount() * multiplier;
        skillManager.addXp(player, Skill.CARPENTRY, xpToGive);
    }
}