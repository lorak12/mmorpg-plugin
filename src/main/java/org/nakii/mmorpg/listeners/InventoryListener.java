package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;

public class InventoryListener implements Listener {

    private final MMORPGCore plugin;

    public InventoryListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    private void scheduleStatRecalculation(Player player) {
        // We delay by one tick to ensure the event has fully processed
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getStatsManager().recalculateStats(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            scheduleStatRecalculation((Player) event.getWhoClicked());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            scheduleStatRecalculation((Player) event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        scheduleStatRecalculation(event.getPlayer());
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        scheduleStatRecalculation(event.getPlayer());
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        scheduleStatRecalculation(event.getPlayer());
    }
}