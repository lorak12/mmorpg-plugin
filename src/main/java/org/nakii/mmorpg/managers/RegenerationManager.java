package org.nakii.mmorpg.managers;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.zone.BlockNode;
import org.nakii.mmorpg.zone.Zone;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RegenerationManager {

    private final MMORPGCore plugin;
    private final ConcurrentHashMap<Location, BukkitTask> activeRegenTasks = new ConcurrentHashMap<>();

    public RegenerationManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void startRegeneration(Block block, BlockNode targetNode) {
        if (targetNode.revertsTo() == null || targetNode.revertTimeSeconds() <= 0) {
            return;
        }

        cancelRegeneration(block.getLocation());

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                activeRegenTasks.remove(block.getLocation()); // Remove task from map once it runs

                // Get the current zone for the block's location using the new WorldManager.
                Zone zone = plugin.getWorldManager().getZoneForLocation(block.getLocation());
                if (zone == null || zone.getFlags().blockBreakingFlags() == null) {
                    // If the block is no longer in a valid regen zone, do nothing.
                    return;
                }

                // Look up the node to revert to from the zone's definitions
                Optional<BlockNode> revertNodeOpt = zone.getEffectiveFlags().blockBreakingFlags()
                        .definitions().values().stream()
                        .filter(node -> node.id().equals(targetNode.revertsTo()))
                        .findFirst();

                revertNodeOpt.ifPresent(revertNode -> block.setType(revertNode.material()));
            }
        }.runTaskLater(plugin, targetNode.revertTimeSeconds() * 20L);

        activeRegenTasks.put(block.getLocation(), task);
    }

    public void cancelRegeneration(Location location) {
        BukkitTask existingTask = activeRegenTasks.remove(location);
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel();
        }
    }

    public void shutdown() {
        plugin.getLogger().info("Cancelling all active block regeneration tasks...");
        activeRegenTasks.values().forEach(BukkitTask::cancel);
        activeRegenTasks.clear();
    }
}