package org.nakii.mmorpg.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class FarmingListener implements Listener {
    private final MMORPGCore plugin;

    public FarmingListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCropHarvest(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        if (event.getBlock().getBlockData() instanceof Ageable) {
            Ageable crop = (Ageable) event.getBlock().getBlockData();
            // Check if the crop is fully grown
            if (crop.getAge() == crop.getMaximumAge()) {
                String materialName = event.getBlock().getType().name();
                double xpToGive = plugin.getSkillManager().getSkillConfig()
                        .getDouble("skills.farming.xp_sources." + materialName, 0.0);

                if (xpToGive > 0) {
                    plugin.getSkillManager().addExperience(event.getPlayer(), Skill.FARMING, xpToGive);
                }
            }
        }
    }
}