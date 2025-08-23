package org.nakii.mmorpg.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.zone.Zone;

public class ForagingListener implements Listener {

    private final MMORPGCore plugin;

    public ForagingListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLogBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // We don't want to grant XP if the player is in creative or if the block is a custom one
        // managed by our more advanced BlockBreakListener.
        Zone zone = plugin.getZoneManager().getZoneForLocation(player.getLocation());
        if (player.getGameMode() == GameMode.CREATIVE || zone != null) {
            return;
        }

        String materialName = block.getType().name();
        if (materialName.endsWith("_LOG")) {
            plugin.getSkillManager().addXpForAction(player, Skill.FORAGING, materialName);
        }
    }
}