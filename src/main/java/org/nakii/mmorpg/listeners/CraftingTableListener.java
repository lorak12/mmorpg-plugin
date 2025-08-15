package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.CraftingGui;

public class CraftingTableListener implements Listener {

    private final MMORPGCore plugin;

    public CraftingTableListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraftingTableInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
            // Cancel the vanilla crafting table and open our custom one.
            event.setCancelled(true);
            new CraftingGui(plugin, event.getPlayer()).open();
        }
    }
}