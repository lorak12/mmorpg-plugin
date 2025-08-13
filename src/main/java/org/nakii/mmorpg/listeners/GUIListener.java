package org.nakii.mmorpg.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.guis.AbstractGui;
import org.nakii.mmorpg.guis.AnvilGui;
import org.nakii.mmorpg.guis.EnchantingGui;

public class GUIListener implements Listener {

    private final MMORPGCore plugin;

    public GUIListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // This is the inventory that was actually clicked
        Inventory clickedInventory = event.getClickedInventory();

        // This is the top inventory of the window
        Inventory topInventory = event.getInventory();
        InventoryHolder holder = topInventory.getHolder();

        // We only care about this event if the holder is our GUI.
        if (holder instanceof AbstractGui) {
            AbstractGui gui = (AbstractGui) holder;

            // If the player clicked in THEIR OWN inventory, we allow it,
            // but schedule an update in case they shift-clicked an item.
            if (clickedInventory != topInventory) {
                // This is a click in the player's inventory.
                if (event.isShiftClick()) {
                    // Schedule a delayed update to allow the item to move.
                    plugin.getServer().getScheduler().runTask(plugin, gui::populateItems);
                }
                // Do not cancel the event, let the player interact with their items.
                return;
            }

            // If we reach here, the click was in the TOP inventory.
            // Now, we pass the event to the GUI's specific handler.
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onGuiClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AbstractGui) {
            Player player = (Player) event.getPlayer();

            // A 1-tick delay is crucial. It lets us check what the player is doing *after* the close event.
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                // ...
                if (holder instanceof EnchantingGui) {
                    returnItem(player, event.getInventory().getItem(19));
                } else if (holder instanceof AnvilGui) {
                    returnItem(player, event.getInventory().getItem(29)); // INPUT_LEFT_SLOT
                    returnItem(player, event.getInventory().getItem(33)); // INPUT_RIGHT_SLOT
                }
            });

            AbstractGui.OPEN_GUIS.remove(player.getUniqueId());
        }
    }
    private void returnItem(Player player, ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            // Give the item to the player, or drop it if their inventory is full.
            if (!player.getInventory().addItem(item).isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }
}