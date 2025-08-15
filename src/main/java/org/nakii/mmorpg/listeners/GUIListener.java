package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.AbstractGui;
import org.nakii.mmorpg.guis.AnvilGui;
import org.nakii.mmorpg.guis.CraftingGui;
import org.nakii.mmorpg.guis.EnchantingGui;

public class GUIListener implements Listener {

    private final MMORPGCore plugin;

    public GUIListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * The main click handler for all custom GUIs.
     * It contains a specialized, robust handler for shift-clicks to prevent item loss.
     */
    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInventory = player.getOpenInventory().getTopInventory();
        if (!(topInventory.getHolder() instanceof AbstractGui gui)) return;

        // --- Robust Shift-Click Handling ---
        // This block manually controls the outcome of shift-clicking an item from the
        // player's inventory into one of our GUIs, preventing all vanilla bugs.
        if (event.getClick().isShiftClick()) {
            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory != null && clickedInventory.equals(player.getInventory())) {

                // 1. Cancel the default server action to take full control.
                event.setCancelled(true);

                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) return;

                // 2. Delegate the item transfer logic to the specific GUI instance.
                ItemStack remaining = gui.handleShiftClick(clickedItem);

                // 3. Update the player's inventory with the remainder.
                // If the item was fully moved, 'remaining' will be null.
                event.setCurrentItem(remaining);

                // 4. Update the GUI's visual state.
                plugin.getServer().getScheduler().runTask(plugin, gui::populateItems);
                return;
            }
        }

        // If it's not a shift-click from the player's inventory, let the GUI's
        // own handleClick method manage the event as usual.
        gui.handleClick(event);
    }

    /**
     * Handles returning items to the player when they close a GUI unexpectedly.
     */
    @EventHandler
    public void onGuiClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AbstractGui) {
            Player player = (Player) event.getPlayer();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // This check is crucial: if the player is now viewing ANOTHER of our custom GUIs,
                // it means it was a state change, not a real close. In that case, do nothing.
                if (player.getOpenInventory().getTopInventory().getHolder() instanceof AbstractGui) {
                    return;
                }

                // If the check fails, the player has truly closed the GUI (e.g., with ESC).
                // Now we can safely return their items.
                if (holder instanceof EnchantingGui) {
                    returnItem(player, event.getInventory().getItem(19));
                } else if (holder instanceof AnvilGui) {
                    returnItem(player, event.getInventory().getItem(29));
                    returnItem(player, event.getInventory().getItem(33));
                } else if (holder instanceof CraftingGui) {
                    int[] inputSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
                    for (int slot : inputSlots) {
                        returnItem(player, event.getInventory().getItem(slot));
                    }
                }
            });

            AbstractGui.OPEN_GUIS.remove(player.getUniqueId());
        }
    }

    /**
     * A helper method to safely give an item to a player, dropping it on the ground
     * if their inventory is full.
     * @param player The player to give the item to.
     * @param item The item to give.
     */
    private void returnItem(Player player, ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            if (!player.getInventory().addItem(item).isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }
}