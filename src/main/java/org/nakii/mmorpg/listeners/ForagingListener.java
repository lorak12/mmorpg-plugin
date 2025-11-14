package org.nakii.mmorpg.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.nakii.mmorpg.managers.SkillManager;
import org.nakii.mmorpg.managers.WorldManager;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.zone.Zone;

public class ForagingListener implements Listener {

    private final SkillManager skillManager;
    private final WorldManager worldManager;

    public ForagingListener(SkillManager skillManager, WorldManager worldManager) {
        this.skillManager = skillManager;
        this.worldManager = worldManager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLogBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Zone zone = worldManager.getZoneForLocation(player.getLocation());
        if (player.getGameMode() == GameMode.CREATIVE || zone != null) {
            return;
        }

        String materialName = block.getType().name();
        if (materialName.endsWith("_LOG")) {
            skillManager.addXpForAction(player, Skill.FORAGING, materialName);
        }
    }
}