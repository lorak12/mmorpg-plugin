package org.nakii.mmorpg.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.managers.RegenerationManager;
import org.nakii.mmorpg.managers.ZoneManager;
import org.nakii.mmorpg.zone.BlockBreakingFlags;
import org.nakii.mmorpg.zone.BlockNode;
import org.nakii.mmorpg.zone.Zone;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class BlockBreakListener implements Listener {

    private final ZoneManager zoneManager;
    private final RegenerationManager regenerationManager;

    public BlockBreakListener(ZoneManager zoneManager, RegenerationManager regenerationManager) {
        this.zoneManager = zoneManager;
        this.regenerationManager = regenerationManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Zone zone = zoneManager.getZoneForLocation(player.getLocation());
        if (zone == null) return;

        BlockBreakingFlags flags = zone.getEffectiveFlags().blockBreakingFlags();
        if (flags == null) return;

        Optional<BlockNode> nodeOpt = flags.findNodeByMaterial(block.getType());

        if (nodeOpt.isEmpty()) {
            if (flags.unlistedBlocksUnbreakable()) {
                event.setCancelled(true);
            }
            return;
        }

        // --- START OF NEW/MODIFIED LOGIC ---

        // Get the tool the player is using to correctly calculate drops
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Before we cancel, calculate what would have dropped
        int expToDrop = event.getExpToDrop();
        Collection<ItemStack> drops;

        if (player.getGameMode() == GameMode.CREATIVE) {
            // Creative mode players get no drops or exp
            drops = null;
            expToDrop = 0;
        } else {
            // This Bukkit method correctly handles Fortune and Silk Touch
            drops = block.getDrops(tool, player);
        }

        // We MUST cancel the event to take control of the block's lifecycle
        event.setCancelled(true);
        // We also manually set exp to 0 to prevent other plugins from interfering
        event.setExpToDrop(0);

        // Give the calculated drops to the player
        if (drops != null) {
            for (ItemStack drop : drops) {
                // Try to add the item to the inventory
                HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(drop);
                // If the inventory is full, drop the rest on the ground
                if (!leftovers.isEmpty()) {
                    leftovers.values().forEach(item -> block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), item));
                }
            }
        }

        // Spawn the experience orb at the block's location
        if (expToDrop > 0) {
            int finalExpToDrop = expToDrop;
            block.getWorld().spawn(block.getLocation().toCenterLocation(), org.bukkit.entity.ExperienceOrb.class, orb -> orb.setExperience(finalExpToDrop));
        }

        // --- END OF NEW/MODIFIED LOGIC ---

        BlockNode currentNode = nodeOpt.get();

        regenerationManager.cancelRegeneration(block.getLocation());

        String breaksToId = currentNode.breaksTo();
        if (breaksToId == null) {
            block.setType(org.bukkit.Material.AIR);
            return;
        }

        BlockNode nextNode = flags.definitions().get(breaksToId);
        if (nextNode == null) {
            block.setType(org.bukkit.Material.AIR);
            System.err.println("Zone '" + zone.getId() + "' has a broken block-breaking chain. Node '" + currentNode.id() + "' points to non-existent node '" + breaksToId + "'.");
            return;
        }

        block.setType(nextNode.material());
        regenerationManager.startRegeneration(block, nextNode);
    }
}