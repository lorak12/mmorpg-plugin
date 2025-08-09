package org.nakii.mmorpg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.nakii.mmorpg.MMORPGCore;

public class MiningListener implements Listener {
    private final MMORPGCore plugin;

    public MiningListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    // In MiningListener.java

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        // We can handle all block-based skills here
        plugin.getSkillManager().handleBlockBreak(event.getPlayer(), event.getBlock().getType());
    }
}