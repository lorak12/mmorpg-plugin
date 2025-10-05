package org.nakii.mmorpg.quest.engine.condition;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;

public class ItemCondition implements QuestCondition {

    private final String itemId;
    private final int amount;
    private final boolean required;

    public ItemCondition(String itemId, int amount, boolean required) {
        this.itemId = itemId;
        this.amount = amount;
        this.required = required;
    }

    @Override
    public boolean check(Player player, MMORPGCore plugin) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            String id = plugin.getItemManager().getItemId(item);
            if (itemId.equalsIgnoreCase(id)) {
                count += item.getAmount();
            }
        }
        boolean hasItems = count >= amount;
        return required ? hasItems : !hasItems;
    }
}