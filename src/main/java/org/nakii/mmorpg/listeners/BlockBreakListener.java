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
import org.nakii.mmorpg.util.ItemDropper;
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
    private final SkillManager skillManager;

    public BlockBreakListener(MMORPGCore plugin, WorldManager worldManager, RegenerationManager regenerationManager, CollectionManager collectionManager, ItemManager itemManager, SkillManager skillManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.regenerationManager = regenerationManager;
        this.collectionManager = collectionManager;
        this.itemManager = itemManager;
        this.skillManager = skillManager;
    }

    public void finishBreaking(Player player, Block block, BlockNode node, PlayerStats stats, BlockBreakingFlags flags) {
        if (node.skillType() != null && node.skillXpReward() > 0) {
            try {
                Skill skill = Skill.valueOf(node.skillType().toUpperCase());
                skillManager.addXp(player, skill, node.skillXpReward());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid skill-type '" + node.skillType() + "' in zone config for node '" + node.id() + "'");
            }
        }

        String collectionId = node.collectionId();
        String dropId = node.customDropId();

        if (dropId == null) {
            Collection<ItemStack> vanillaDrops = block.getDrops(player.getInventory().getItemInMainHand(), player);
            if (!vanillaDrops.isEmpty()) {
                ItemStack firstDrop = vanillaDrops.iterator().next();
                ItemStack finalDrop = itemManager.createItemStack(firstDrop.getType().name());
                if (finalDrop == null) {
                    finalDrop = firstDrop;
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

        String breaksToId = node.breaksTo();
        if (breaksToId != null && flags.definitions().containsKey(breaksToId)) {
            BlockNode nextNode = flags.definitions().get(breaksToId);
            block.setType(nextNode.material());
            regenerationManager.startRegeneration(block, nextNode);
        } else {
            block.setType(Material.BEDROCK); // Change to bedrock instead of air
            regenerationManager.startRegeneration(block, node);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        CustomWorld customWorld = worldManager.getCustomWorld(player.getWorld().getName());
        if (customWorld != null && !customWorld.getFlags().canBreakBlocks()) {
            event.setCancelled(true);
            return;
        }

        Zone zone = worldManager.getZoneForLocation(event.getBlock().getLocation());
        if (zone == null) {
            return;
        }

        BlockBreakingFlags flags = zone.getFlags().blockBreakingFlags();
        if (flags != null) {
            Optional<BlockNode> nodeOpt = flags.findNodeByMaterial(event.getBlock().getType());
            if (nodeOpt.isPresent()) {
                event.setCancelled(true);
                return;
            }
            if (flags.unlistedBlocksUnbreakable()) {
                event.setCancelled(true);
            }
        }
    }
}