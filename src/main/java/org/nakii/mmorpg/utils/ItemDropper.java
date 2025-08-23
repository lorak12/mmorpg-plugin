package org.nakii.mmorpg.utils;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.ItemManager;

/**
 * A utility for handling player-specific item drops.
 */
public class ItemDropper {

    /**
     * Spawns an item in the world that is only visible and pickup-able by the owner.
     * The item is marked as "pristine" for collection tracking.
     *
     * @param owner The player who will own the drop.
     * @param loc The location to spawn the item at.
     * @param itemStack The ItemStack to drop.
     */
    public static void dropPristineItem(Player owner, Location loc, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return;
        }

        // Mark the item with the pristine NBT tag
        itemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(ItemManager.PRISTINE_KEY, PersistentDataType.BYTE, (byte) 1);
        });

        // Spawn the item entity
        loc.getWorld().spawn(loc, Item.class, item -> {
            item.setItemStack(itemStack);
            item.setOwner(owner.getUniqueId()); // Only the owner can pick it up
            item.setGlowing(true); // Make it glow

            // Paper API feature: make the item invisible to all other players
            for (Player otherPlayer : loc.getWorld().getPlayers()) {
                if (!otherPlayer.equals(owner)) {
                    otherPlayer.hideEntity(MMORPGCore.getInstance(), item);
                }
            }
        });
    }
}