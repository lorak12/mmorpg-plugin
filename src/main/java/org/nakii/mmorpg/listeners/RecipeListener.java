package org.nakii.mmorpg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;

public class RecipeListener implements Listener {

    private final MMORPGCore plugin;

    public RecipeListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (event.getInventory().getResult() != null) {
            // Let vanilla or other plugins handle it if there's already a result
            return;
        }

        ItemStack result = plugin.getRecipeManager().checkCraftingMatrix(event.getInventory());
        if (result != null) {
            event.getInventory().setResult(result);
        }
    }
}