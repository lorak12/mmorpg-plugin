package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.SmithItemEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class ForagingListener implements Listener {
    private final MMORPGCore plugin;

    public ForagingListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSmith(SmithItemEvent event) {
        if (event.isCancelled()) return;

        Player player = (Player) event.getWhoClicked();
        String materialName = event.getCurrentItem().getType().name();

        double xpToGive = plugin.getSkillManager().getSkillConfig()
                .getDouble("skills.forging.xp_sources." + materialName, 0.0);

        if (xpToGive > 0) {
            plugin.getSkillManager().addExperience(player, Skill.FORAGING, xpToGive);
        }
    }
}