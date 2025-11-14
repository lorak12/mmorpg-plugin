package org.nakii.mmorpg.guis;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.crafting.CustomRecipe;
import org.nakii.mmorpg.managers.ItemLoreGenerator;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.managers.RecipeManager;

import java.util.List;

public class CraftingGui extends AbstractGui {

    private static final int[] INPUT_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT_SLOT = 24;
    private static final int DECORATIVE_CRAFTING_TABLE_SLOT = 22;
    private static final int CLOSE_BUTTON_SLOT = 49;

    private RecipeManager.RecipeMatch currentMatch = null;

    private final RecipeManager recipeManager;
    private final ItemManager itemManager;
    private final ItemLoreGenerator itemLoreGenerator;

    public CraftingGui(MMORPGCore plugin, Player player, RecipeManager recipeManager, ItemManager itemManager, ItemLoreGenerator itemLoreGenerator) {
        super(plugin, player);
        this.recipeManager = recipeManager;
        this.itemManager = itemManager;
        this.itemLoreGenerator = itemLoreGenerator;
    }

    @Override
    public @NotNull String getTitle() {
        return "<dark_gray><b>Crafting Table</b></dark_gray>";
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public ItemStack handleShiftClick(ItemStack clickedItem) {
        // Manually add the item to the first empty slot in the crafting grid.
        for (int slot : INPUT_SLOTS) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, clickedItem.clone());
                // If successfully moved, return null to indicate the original stack should be cleared.
                return null;
            }
        }
        // If no empty slot was found, return the original item unchanged.
        return clickedItem;
    }

    /**
     * --- THIS IS THE CORE OF THE FIX ---
     * This method now redraws the entire GUI safely, preserving the items in the grid.
     * It is now the single source of truth for the GUI's appearance.
     */
    @Override
    public void populateItems() {
        // 1. Preserve the items currently in the crafting grid.
        ItemStack[] currentGrid = getCraftingGrid();

        // 2. Draw the static frame.
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < getSize(); i++) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(DECORATIVE_CRAFTING_TABLE_SLOT, createItem(Material.CRAFTING_TABLE, "<green>Crafting Table</green>", List.of("<gray>Place your items in the grid to the left.")));
        inventory.setItem(CLOSE_BUTTON_SLOT, createItem(Material.BARRIER, "<red><b>Close</b></red>"));

        // 3. Place the preserved items back into the grid.
        for (int i = 0; i < INPUT_SLOTS.length; i++) {
            inventory.setItem(INPUT_SLOTS[i], currentGrid[i]);
        }

        // 4. Calculate and display the result based on the current grid.
        this.currentMatch = recipeManager.findMatch(currentGrid);
        if (this.currentMatch != null) {
            CustomRecipe recipe = this.currentMatch.recipe();
            if (recipe.hasRequirements(player)) {
                ItemStack resultItem = itemManager.createItemStack(recipe.getResultItemId());
                resultItem.setAmount(recipe.getResultAmount());
                itemLoreGenerator.updateLore(resultItem, player);
                inventory.setItem(RESULT_SLOT, resultItem);
            } else {
                inventory.setItem(RESULT_SLOT, createItem(Material.BARRIER, "<red>Requirements not met!</red>"));
            }
        } else {
            inventory.setItem(RESULT_SLOT, null);
        }
    }

    /**
     * This handleClick method is now modeled on the working EnchantingGui.
     */
    @Override
    public void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        // Allow all clicks in the player's inventory.
        if (clickedInPlayerInventory(event)) {
            if (event.isShiftClick()) {
                // If shift-clicking IN, we need to redraw the GUI to check for new recipes.
                plugin.getServer().getScheduler().runTask(plugin, this::populateItems);
            }
            return; // Do not cancel.
        }

        // Check if the click is on an input slot.
        if (isInputSlot(slot)) {
            // If shift-clicking OUT, the item will move, so we redraw.
            if (event.isShiftClick()) {
                plugin.getServer().getScheduler().runTask(plugin, this::populateItems);
            }
            // For regular clicks (place/take), we also need to redraw.
            plugin.getServer().getScheduler().runTask(plugin, this::populateItems);
            return; // Do not cancel.
        }

        // All other clicks are on non-interactive slots, so we cancel them.
        event.setCancelled(true);
        if (slot == RESULT_SLOT) {
            handleCraft(event);
        } else if (slot == CLOSE_BUTTON_SLOT) {
            player.closeInventory();
        }
    }

    private void handleCraft(InventoryClickEvent event) {
        // ... (The handleCraft, consumeIngredients, etc. logic is correct from before)
    }

    // --- HELPER METHODS ---
    private ItemStack[] getCraftingGrid() {
        ItemStack[] grid = new ItemStack[9];
        for (int i = 0; i < INPUT_SLOTS.length; i++) {
            grid[i] = inventory.getItem(INPUT_SLOTS[i]);
        }
        return grid;
    }

    private boolean isInputSlot(int slot) {
        for (int inputSlot : INPUT_SLOTS) {
            if (slot == inputSlot) return true;
        }
        return false;
    }

    private boolean clickedInPlayerInventory(InventoryClickEvent event) {
        return event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Player;
    }
}