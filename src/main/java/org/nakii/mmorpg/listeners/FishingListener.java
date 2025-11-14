package org.nakii.mmorpg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.nakii.mmorpg.managers.SkillManager;
import org.nakii.mmorpg.skills.Skill;

public class FishingListener implements Listener {

    private final SkillManager skillManager;

    public FishingListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            skillManager.addXpForAction(event.getPlayer(), Skill.FISHING, "DEFAULT_CATCH");
        }
    }
}