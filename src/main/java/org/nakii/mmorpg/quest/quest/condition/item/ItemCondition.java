package org.nakii.mmorpg.quest.quest.condition.item;

import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.item.QuestItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Stream;

/**
 * A condition that checks if the player has the specified items.
 */
public class ItemCondition implements OnlineCondition {

    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * The items to check for.
     */
    private final Variable<List<Item>> items;

    /**
     * Create a new item condition.
     *
     * @param items       the items to check for
     * @param dataStorage the storage providing player data
     */
    public ItemCondition(final Variable<List<Item>> items, final PlayerDataStorage dataStorage) {
        this.items = items;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        final ItemStack[] inventoryItems = profile.getPlayer().getInventory().getContents();
        final List<ItemStack> backpackItems = dataStorage.get(profile).getBackpack();

        for (final Item item : items.getValue(profile)) {
            final QuestItem questItem = item.getItem(profile);
            final long totalAmount = Stream.concat(
                            Stream.of(inventoryItems),
                            backpackItems.stream()
                    )
                    .filter(questItem::matches)
                    .mapToInt(ItemStack::getAmount)
                    .sum();
            if (totalAmount < item.getAmount().getValue(profile).intValue()) {
                return false;
            }
        }
        return true;
    }
}
