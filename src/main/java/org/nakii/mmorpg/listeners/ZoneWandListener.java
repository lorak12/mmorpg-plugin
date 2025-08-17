package org.nakii.mmorpg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nakii.mmorpg.utils.ZoneWand;

public class ZoneWandListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!ZoneWand.isWand(event.getItem())) {
            return;
        }

        // Prevent the wand from doing anything else (like placing blocks if it were a block)
        event.setCancelled(true);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            ZoneWand.addPoint(event.getPlayer(), event.getClickedBlock().getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ZoneWand.clearPoints(event.getPlayer());
        }
    }
}