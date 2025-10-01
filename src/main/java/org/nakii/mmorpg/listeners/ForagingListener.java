package org.nakii.mmorpg.listeners;

import org.bukkit.GameMode;
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

        // We get the zone from the new WorldManager.
        // The logic is to prevent giving vanilla XP if the block is in a custom zone,
        // which might have its own XP rules defined in a BlockNode.
        Zone zone = plugin.getWorldManager().getZoneForLocation(player.getLocation());
        if (player.getGameMode() == GameMode.CREATIVE || zone != null) {
            return;
        }

        String materialName = block.getType().name();
        if (materialName.endsWith("_LOG")) {
            plugin.getSkillManager().addXpForAction(player, Skill.FORAGING, materialName);
        }
    }
}