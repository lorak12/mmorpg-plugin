package org.nakii.mmorpg.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class MiningListener implements Listener {
    private final MMORPGCore plugin;

    public MiningListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        Block block = event.getBlock();
        String materialName = block.getType().name();

        double xpToGive = plugin.getSkillManager().getSkillConfig()
                .getDouble("skills.mining.xp_sources." + materialName, 0.0);

        if (xpToGive > 0) {
            plugin.getSkillManager().addExperience(player, Skill.MINING, xpToGive);
        }
    }
}