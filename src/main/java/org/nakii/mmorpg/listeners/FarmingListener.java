package org.nakii.mmorpg.listeners;

import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class FarmingListener implements Listener {
    private final MMORPGCore plugin;
    public FarmingListener(MMORPGCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        if (event.getBlock().getBlockData() instanceof Ageable crop) {
            if (crop.getAge() == crop.getMaximumAge()) {
                plugin.getSkillManager().addXpForAction(event.getPlayer(), Skill.FARMING, event.getBlock().getType().name());
            }
        }
    }
}