package org.nakii.mmorpg.listeners.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.listeners.BlockBreakListener;
import org.nakii.mmorpg.managers.StatsManager;
import org.nakii.mmorpg.managers.WorldManager;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.zone.BlockBreakingFlags;
import org.nakii.mmorpg.zone.BlockNode;
import org.nakii.mmorpg.zone.Zone;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CustomMiningPacketListener extends PacketAdapter {

    private final MMORPGCore plugin;
    private final Map<UUID, BukkitRunnable> activeMiningTasks = new HashMap<>();
    private final WorldManager worldManager;
    private final StatsManager statsManager;
    private final BlockBreakListener blockBreakListener;

    public CustomMiningPacketListener(MMORPGCore plugin, BlockBreakListener blockBreakListener, WorldManager worldManager, StatsManager statsManager) {
        super(plugin, PacketType.Play.Client.BLOCK_DIG);
        this.plugin = plugin;
        this.blockBreakListener = blockBreakListener;
        this.worldManager = worldManager;
        this.statsManager = statsManager;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        EnumWrappers.PlayerDigType digType = event.getPacket().getPlayerDigTypes().read(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                if (digType == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
                    // By cancelling the "start" packet, we prevent the client from
                    // ever starting its own breaking animation. Only our packets will be shown.
                    event.setCancelled(true);

                    BlockPosition blockPos = event.getPacket().getBlockPositionModifier().read(0);
                    Block block = blockPos.toLocation(player.getWorld()).getBlock();
                    handleStartDigging(player, block);
                } else if (digType == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK || digType == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
                    handleStopDigging(player);
                }
            }
        }.runTask(plugin);
    }

    private void handleStartDigging(Player player, Block block) {
        handleStopDigging(player); // Cancel any previous task

        // --- DEBUG MESSAGES START ---

        Zone zone = worldManager.getZoneForLocation(player.getLocation());
        if (zone == null) {
            return;
        }

        BlockBreakingFlags flags = zone.getFlags().blockBreakingFlags();
        if (flags == null) {
            return;
        }

        Optional<BlockNode> nodeOpt = flags.findNodeByMaterial(block.getType());
        if (nodeOpt.isEmpty()) {
            // If unlisted blocks are unbreakable, we should also prevent the vanilla breaking animation.
            if (flags.unlistedBlocksUnbreakable()) {
                player.sendBlockDamage(block.getLocation(), 1.0f); // Instantly "repair" the block to stop animation
            }
            return;
        }
        // --- DEBUG MESSAGES END ---


        BlockNode node = nodeOpt.get();
        PlayerStats stats = statsManager.getStats(player);

        if (stats.getStat(Stat.BREAKING_POWER) < node.breakingPowerRequired()) {
            return;
        }

        double calculatedBreakTimeTicks = node.baseBreakTimeSeconds() * 20 * (1 - (stats.getStat(Stat.MINING_SPEED) / 1000.0));
        if (calculatedBreakTimeTicks < 1) calculatedBreakTimeTicks = 1; // Prevent division by zero
        final double finalBreakTimeTicks = calculatedBreakTimeTicks;


        BukkitRunnable miningTask = new BukkitRunnable() {
            private int ticksElapsed = 0;
            @Override
            public void run() {
                ticksElapsed += 2; // Check every 2 ticks for less network spam
                player.sendBlockDamage(block.getLocation(), (float) ticksElapsed / (float) finalBreakTimeTicks);
                if (ticksElapsed >= finalBreakTimeTicks) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            blockBreakListener.finishBreaking(player, block, node, stats, flags);
                        }
                    }.runTask(plugin);
                    cancel();
                }
            }
        };
        miningTask.runTaskTimer(plugin, 0L, 2L); // Run every 2 ticks
        activeMiningTasks.put(player.getUniqueId(), miningTask);
    }

    private void handleStopDigging(Player player) {
        BukkitRunnable task = activeMiningTasks.remove(player.getUniqueId());
        if (task != null) {
            try {
                task.cancel();
                // We'll need to clear the crack animation here later.
            } catch (IllegalStateException ignored) {}
        }
    }
}