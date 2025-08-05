package org.nakii.mmorpg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class FishingListener implements Listener {
    private final MMORPGCore plugin;

    public FishingListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            double xpToGive = plugin.getSkillManager().getSkillConfig()
                    .getDouble("skills.fishing.xp_sources.DEFAULT", 0.0);

            if (xpToGive > 0) {
                plugin.getSkillManager().addExperience(event.getPlayer(), Skill.FISHING, xpToGive);
            }
        }
    }
}
