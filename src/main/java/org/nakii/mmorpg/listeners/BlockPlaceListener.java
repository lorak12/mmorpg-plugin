package org.nakii.mmorpg.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.WorldManager;
import org.nakii.mmorpg.world.CustomWorld;

public class BlockPlaceListener implements Listener {

    private final WorldManager worldManager;

    public BlockPlaceListener(MMORPGCore plugin) {
        this.worldManager = plugin.getWorldManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        // Only bypass rules if the player is an OP AND in Creative mode.
        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        CustomWorld customWorld = worldManager.getCustomWorld(player.getWorld().getName());
        if (customWorld != null && !customWorld.getFlags().canPlaceBlocks()) {
            event.setCancelled(true);
            // Optionally send a message
             player.sendMessage(Component.text("You can't place blocks in this world.", NamedTextColor.RED));
        }
    }
}