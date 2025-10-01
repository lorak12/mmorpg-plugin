package org.nakii.mmorpg.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.*;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.utils.ItemDropper;
import org.nakii.mmorpg.zone.BlockBreakingFlags;
import org.nakii.mmorpg.zone.BlockNode;
import org.nakii.mmorpg.zone.Zone;
import org.nakii.mmorpg.world.CustomWorld;

import java.util.Collection;
import java.util.Optional;

public class BlockBreakListener implements Listener {

    private final MMORPGCore plugin;
    private final WorldManager worldManager;
    private final RegenerationManager regenerationManager;
    private final CollectionManager collectionManager;
    private final ItemManager itemManager;

    public BlockBreakListener(MMORPGCore plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        this.regenerationManager = plugin.getRegenerationManager();
        this.collectionManager = plugin.getCollectionManager();
        this.itemManager = plugin.getItemManager();
    }

    /**
     * Called by the CustomMiningPacketListener after a custom break finishes.
     * This is where all rewards and block state changes happen.
     */
    public void finishBreaking(Player player, Block block, BlockNode node, PlayerStats stats, BlockBreakingFlags flags) {

        // --- 1. Grant Skill XP ---
        if (node.skillType() != null && node.skillXpReward() > 0) {
            try {
                Skill skill = Skill.valueOf(node.skillType().toUpperCase());
                plugin.getSkillManager().addXp(player, skill, node.skillXpReward());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid skill-type '" + node.skillType() + "' in zone config for node '" + node.id() + "'");
            }
        }

        // --- 2. Calculate Drops & Grant Collection Progress ---
        String collectionId = node.collectionId();
        String dropId = node.customDropId();

        if (dropId == null) {
            // --- THIS IS THE NEW, MORE ROBUST VANILLA DROP LOGIC ---
            Collection<ItemStack> vanillaDrops = block.getDrops(player.getInventory().getItemInMainHand(), player);
            if (!vanillaDrops.isEmpty()) {
                ItemStack firstDrop = vanillaDrops.iterator().next();
                // We use the custom item system as a fallback, but prioritize the actual vanilla item.
                ItemStack finalDrop = itemManager.createItemStack(firstDrop.getType().name());
                if (finalDrop == null) {
                    finalDrop = firstDrop; // Use the actual vanilla ItemStack
                }

                double fortuneStat = stats.getStat(Stat.MINING_FORTUNE);
                int finalAmount = finalDrop.getAmount() + (int) (fortuneStat / 100.0);
                if (Math.random() < (fortuneStat % 100) / 100.0) finalAmount++;

                finalDrop.setAmount(finalAmount);
                ItemDropper.dropPristineItem(player, block.getLocation().toCenterLocation(), finalDrop);

                if (collectionId != null) {
                    collectionManager.addProgress(player, collectionId, finalDrop.getAmount());
                }
            }
        } else {
            // --- This is the logic for custom drops (e.g., from a custom item ID) ---
            double fortuneStat = stats.getStat(Stat.MINING_FORTUNE);
            int finalAmount = 1 + (int) (fortuneStat / 100.0);
            if (Math.random() < (fortuneStat % 100) / 100.0) finalAmount++;

            ItemStack finalDrop = itemManager.createItemStack(dropId);
            if (finalDrop != null) {
                finalDrop.setAmount(finalAmount);
                ItemDropper.dropPristineItem(player, block.getLocation().toCenterLocation(), finalDrop);
                if (collectionId != null) {
                    collectionManager.addProgress(player, collectionId, finalDrop.getAmount());
                }
            }
        }


        // --- 3. Handle Block State Change and Regeneration (No changes here) ---
        String breaksToId = node.breaksTo();
        if (breaksToId != null && flags.definitions().containsKey(breaksToId)) {
            BlockNode nextNode = flags.definitions().get(breaksToId);
            block.setType(nextNode.material());
            regenerationManager.startRegeneration(block, nextNode);
        } else {
            block.setType(Material.AIR);
            regenerationManager.startRegeneration(block, node);
        }
    }

    /**
     * Prevents vanilla block breaking based on world and zone rules.
     * Runs at HIGHEST priority to ensure our rules are final.
     */
    /**
     * Prevents vanilla block breaking based on world and zone rules.
     * Runs at HIGHEST priority to ensure our rules are final.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        // Allow ops in creative to bypass all checks.
        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check the world-level rules first.
        CustomWorld customWorld = worldManager.getCustomWorld(player.getWorld().getName());
        if (customWorld != null && !customWorld.getFlags().canBreakBlocks()) {
            event.setCancelled(true);
            return; // World rule forbids all breaking.
        }

        // Now check zone-specific rules.
        Zone zone = worldManager.getZoneForLocation(event.getBlock().getLocation());
        if (zone == null) {
            return; // In the wilderness, controlled by world flags (which we already checked).
        }

        BlockBreakingFlags flags = zone.getFlags().blockBreakingFlags();
        if (flags != null) {
            Optional<BlockNode> nodeOpt = flags.findNodeByMaterial(event.getBlock().getType());
            // If the block is part of our custom system, always cancel the vanilla event.
            // The packet listener will take over.
            if (nodeOpt.isPresent()) {
                event.setCancelled(true);
                return;
            }
            // If the block is not in our system, but the zone is strict, cancel.
            if (flags.unlistedBlocksUnbreakable()) {
                event.setCancelled(true);
            }
        }
    }
}