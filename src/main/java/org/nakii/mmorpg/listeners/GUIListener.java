package org.nakii.mmorpg.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.GUIManager;

public class GUIListener implements Listener {
    private final MMORPGCore plugin;

    public GUIListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        // Check if the inventory is one of our GUIs
        if (event.getInventory().getHolder() instanceof GUIManager) {
            // Prevent players from taking items out of the GUI
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            // Check for our skill_id tag
            String skillId = clickedItem.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey(plugin, "skill_id"), PersistentDataType.STRING);

            if (skillId != null) {
                // This is a skill icon. For now, we can just close the menu as an example.
                // In a future step, this would open a detailed view.
                player.sendMessage("You clicked on the " + skillId + " skill!");
                player.closeInventory();
            }
        }
    }
}