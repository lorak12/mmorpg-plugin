package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.CraftingGui;
import org.nakii.mmorpg.managers.ItemLoreGenerator;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.managers.RecipeManager;

public class CraftingTableListener implements Listener {

    private final MMORPGCore plugin;
    private final RecipeManager recipeManager;
    private final ItemManager itemManager;
    private final ItemLoreGenerator itemLoreGenerator;

    public CraftingTableListener(MMORPGCore plugin, RecipeManager recipeManager, ItemManager itemManager, ItemLoreGenerator itemLoreGenerator) {
        this.plugin = plugin;
        this.recipeManager = recipeManager;
        this.itemManager = itemManager;
        this.itemLoreGenerator = itemLoreGenerator;
    }

    @EventHandler
    public void onCraftingTableInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
            event.setCancelled(true);
            new CraftingGui(plugin, event.getPlayer(),recipeManager, itemManager, itemLoreGenerator).open();
        }
    }
}