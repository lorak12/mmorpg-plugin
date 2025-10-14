package org.nakii.mmorpg.quest.quest.objective.pickup;

import org.nakii.mmorpg.quest.api.CountingObjective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents an objective that is completed when a player picks up a specific item.
 */
public class PickupObjective extends CountingObjective implements Listener {

    /**
     * The target amount of items to be picked up.
     */
    private final Variable<List<Item>> pickupItems;

    /**
     * Constructor for the PickupObjective.
     *
     * @param instruction  the instruction that created this objective
     * @param targetAmount the target amount of items to be picked up
     * @param pickupItems  the items to be picked up
     * @throws QuestException if there is an error in the instruction
     */
    public PickupObjective(final Instruction instruction, final Variable<Number> targetAmount,
                           final Variable<List<Item>> pickupItems) throws QuestException {
        super(instruction, targetAmount, "items_to_pickup");
        this.pickupItems = pickupItems;
    }

    /**
     * Handles when the player picks up an item.
     *
     * @param event The event object.
     */
    @EventHandler(ignoreCancelled = true)
    public void onPickup(final EntityPickupItemEvent event) {
        if (event.getEntity() instanceof final Player player) {
            final OnlineProfile onlineProfile = profileProvider.getProfile(player);
            qeHandler.handle(() -> {
                if (containsPlayer(onlineProfile)
                        && isValidItem(onlineProfile, event.getItem().getItemStack())
                        && checkConditions(onlineProfile)) {
                    final ItemStack pickupItem = event.getItem().getItemStack();
                    getCountingData(onlineProfile).progress(pickupItem.getAmount());
                    completeIfDoneOrNotify(onlineProfile);
                }
            });
        }
    }

    private boolean isValidItem(final OnlineProfile onlineProfile, final ItemStack itemStack) throws QuestException {
        for (final Item item : pickupItems.getValue(onlineProfile)) {
            if (item.matches(itemStack, onlineProfile)) {
                return true;
            }
        }
        return false;
    }
}
