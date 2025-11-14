package org.nakii.mmorpg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.managers.ItemManager;
import org.nakii.mmorpg.util.Keys;

/**
 * Manages the lifecycle of "pristine" items to prevent collection exploits.
 */
public class PristineItemListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Player)) {
            return;
        }

        ItemStack itemStack = event.getItem().getItemStack();
        if (itemStack.hasItemMeta()) {
            // Check if the item has the pristine tag
            if (itemStack.getItemMeta().getPersistentDataContainer().has(Keys.PRISTINE_ITEM, PersistentDataType.BYTE)) {
                // It's pristine. We must now "consume" the tag by removing it.
                // We edit the meta of the stack *before* it enters the player's inventory.
                itemStack.editMeta(meta -> {
                    meta.getPersistentDataContainer().remove(Keys.PRISTINE_ITEM);
                });
            }
        }
    }
}