package org.nakii.mmorpg.quest.engine.event;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.model.QuestPackage;

/**
 * Event that gives a custom item to a player.
 */
public class GiveItemEvent implements QuestEvent {

    private final String itemId;
    private final int amount;

    public GiveItemEvent(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    @Override
    public void execute(Player player, MMORPGCore plugin, QuestPackage context) { // <-- Signature changed
        ItemStack item = plugin.getItemManager().createItemStack(itemId);
        if (item == null) {
            plugin.getLogger().warning("Quest event failed: Could not create item with ID '" + itemId + "'.");
            return;
        }
        item.setAmount(amount);
        player.getInventory().addItem(item);
    }
}