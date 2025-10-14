package org.nakii.mmorpg.quest.quest.condition.hand;

import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;
import org.bukkit.inventory.PlayerInventory;

/**
 * Condition to check if a player is holding an item in their hand.
 */
public class HandCondition implements OnlineCondition {

    /**
     * The item to check for.
     */
    private final Variable<Item> item;

    /**
     * Whether the item is in the offhand.
     */
    private final boolean offhand;

    /**
     * Creates a new hand condition.
     *
     * @param item    the item to check for
     * @param offhand whether the item is in the offhand
     */
    public HandCondition(final Variable<Item> item, final boolean offhand) {
        this.item = item;
        this.offhand = offhand;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        final PlayerInventory inv = profile.getPlayer().getInventory();
        return item.getValue(profile).matches(offhand ? inv.getItemInOffHand() : inv.getItemInMainHand(), profile);
    }
}
