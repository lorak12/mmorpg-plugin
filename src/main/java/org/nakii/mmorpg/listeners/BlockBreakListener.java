package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.*;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.utils.ItemDropper;
import org.nakii.mmorpg.zone.BlockBreakingFlags;
import org.nakii.mmorpg.zone.BlockNode;

import java.util.*;

public class BlockBreakListener implements Listener {

    private final MMORPGCore plugin;
    private final ZoneManager zoneManager;
    private final RegenerationManager regenerationManager;
    private final CollectionManager collectionManager;
    private final StatsManager statsManager;
    private final ItemManager itemManager; // Add this
    private final Map<UUID, BukkitRunnable> activeMiningTasks = new HashMap<>();

    public BlockBreakListener(MMORPGCore plugin) {
        this.plugin = plugin;
        this.zoneManager = plugin.getZoneManager();
        this.regenerationManager = plugin.getRegenerationManager();
        this.collectionManager = plugin.getCollectionManager();
        this.statsManager = plugin.getStatsManager();
        this.itemManager = plugin.getItemManager(); // Initialize this
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // The packet system now controls all custom timed breaking.
        // We only cancel the event here to be absolutely sure players cannot break blocks normally.
        // This stops vanilla drops for blocks we don't manage in zones.
        if (zoneManager.getZoneForLocation(event.getPlayer().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

    public void finishBreaking(Player player, Block block, BlockNode node, PlayerStats stats, BlockBreakingFlags flags) {
        cancelMiningTask(player);
        player.sendBlockDamage(block.getLocation(), 0.0f);

        // --- 1. Handle Skill XP ---
        // This is now clean, direct, and unambiguous.
        if (node.skillType() != null && node.skillXpReward() > 0) {
            try {
                Skill skill = Skill.valueOf(node.skillType().toUpperCase());
                plugin.getSkillManager().addXp(player, skill, node.skillXpReward());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid skill-type '" + node.skillType() + "' in zone config for node '" + node.id() + "'");
            }
        }

        // --- 2. Calculate Drops & Grant Collection Progress ---
        // This part of the logic is already correct and does not need to change.
        String collectionId = node.collectionId();
        String dropId = node.customDropId();

        if (dropId == null) {
            Collection<ItemStack> vanillaDrops = block.getDrops(player.getInventory().getItemInMainHand(), player);
            if (!vanillaDrops.isEmpty()) {
                dropId = vanillaDrops.iterator().next().getType().name();
            }
        }

        if (dropId != null) {
            double fortuneStat = stats.getStat(Stat.MINING_FORTUNE); // Or Foraging Fortune, etc.
            int finalAmount = 1;
            finalAmount += (int) (fortuneStat / 100.0);
            if (Math.random() < (fortuneStat % 100) / 100.0) finalAmount++;

            ItemStack finalDrop = itemManager.createItemStack(dropId);
            if(finalDrop == null) {
                finalDrop = itemManager.createDefaultItemStack(Material.matchMaterial(dropId));
            }

            if (finalDrop != null) {
                finalDrop.setAmount(finalAmount);
                if (collectionId != null) {
                    collectionManager.addProgress(player, collectionId, finalDrop.getAmount());
                }
                ItemDropper.dropPristineItem(player, block.getLocation().toCenterLocation(), finalDrop);
            }
        }

        // --- 3. Handle Regeneration ---
        handleRegeneration(block, node, flags);
    }

    // Add a handler to cancel mining when a player quits
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelMiningTask(event.getPlayer());
    }

    // --- ADD THIS NEW HELPER METHOD ---
    /**
     * A dedicated helper to handle the block regeneration state change.
     */
    private void handleRegeneration(Block block, BlockNode node, BlockBreakingFlags flags) {
        regenerationManager.cancelRegeneration(block.getLocation());
        String breaksToId = node.breaksTo();
        if (breaksToId == null) {
            block.setType(Material.AIR);
            return;
        }

        BlockNode nextNode = flags.definitions().get(breaksToId);
        if (nextNode != null) {
            block.setType(nextNode.material());
            regenerationManager.startRegeneration(block, nextNode);
        } else {
            block.setType(Material.AIR);
            System.err.println("Zone has a broken block-breaking chain. Node '" + node.id() + "' points to non-existent node '" + breaksToId + "'.");
        }
    }

    private void cancelMiningTask(Player player) {
        BukkitRunnable task = activeMiningTasks.remove(player.getUniqueId());
        if (task != null) {
            try {
                task.cancel();
            } catch (IllegalStateException ignored) {
                // Task may have already finished and cancelled itself.
            }
        }
    }
}