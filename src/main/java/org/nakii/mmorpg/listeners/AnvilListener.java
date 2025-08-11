package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.AnvilGui;

public class AnvilListener implements Listener {

    private final MMORPGCore plugin;

    public AnvilListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Material type = clickedBlock.getType();
        if (type != Material.ANVIL && type != Material.CHIPPED_ANVIL && type != Material.DAMAGED_ANVIL) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        new AnvilGui(plugin, player).open();
    }
}