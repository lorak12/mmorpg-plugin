package org.nakii.mmorpg.quest.quest.objective.consume;

import org.nakii.mmorpg.quest.api.CountingObjective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * Requires the player to consume an item (eat food or drink a potion).
 */
public class ConsumeObjective extends CountingObjective implements Listener {

    /**
     * The item to consume.
     */
    private final Variable<Item> item;

    /**
     * Constructs a new {@code ConsumeObjective} for the given {@code Instruction}.
     *
     * @param instruction  the instruction out of a quest package
     * @param targetAmount the amount of items to consume
     * @param item         the item to consume
     * @throws QuestException if the instruction is invalid
     */
    public ConsumeObjective(final Instruction instruction, final Variable<Number> targetAmount,
                            final Variable<Item> item) throws QuestException {
        super(instruction, targetAmount, null);
        this.item = item;
    }

    /**
     * The listener that handles a consumed item.
     *
     * @param event the Bukkit event for consuming an item
     */
    @EventHandler(ignoreCancelled = true)
    public void onConsume(final PlayerItemConsumeEvent event) {
        final OnlineProfile onlineProfile = profileProvider.getProfile(event.getPlayer());
        qeHandler.handle(() -> {
            if (containsPlayer(onlineProfile)
                    && item.getValue(onlineProfile).matches(event.getItem(), onlineProfile)
                    && checkConditions(onlineProfile)) {
                getCountingData(onlineProfile).progress();
                completeIfDoneOrNotify(onlineProfile);
            }
        });
    }
}
