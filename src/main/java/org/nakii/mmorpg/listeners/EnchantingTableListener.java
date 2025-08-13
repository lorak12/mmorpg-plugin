package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.EnchantingGui;

public class EnchantingTableListener implements Listener {

    private final MMORPGCore plugin;

    public EnchantingTableListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.ENCHANTING_TABLE) return;

        // We have confirmed the player right-clicked an enchanting table.
        // Cancel the vanilla GUI and open our custom one.
        event.setCancelled(true);
        Player player = event.getPlayer();

        // Open our new custom GUI instead of the old GUIManager method
        new EnchantingGui(plugin, player, clickedBlock).open();
    }
}