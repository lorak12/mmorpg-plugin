package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class CraftingListener implements Listener {
    private final MMORPGCore plugin;

    public CraftingListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.isCancelled()) return;

        Player player = (Player) event.getWhoClicked();
        String materialName = event.getRecipe().getResult().getType().name();

        double xpToGive = plugin.getSkillManager().getSkillConfig()
                .getDouble("skills.crafting.xp_sources." + materialName,
                        plugin.getSkillManager().getSkillConfig().getDouble("skills.crafting.xp_sources.DEFAULT", 0.0));

        if (xpToGive > 0) {
            plugin.getSkillManager().addExperience(player, Skill.CRAFTING, xpToGive);
        }
    }
}