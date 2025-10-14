package org.nakii.mmorpg.quest.quest.event.hunger;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEvent;
import org.bukkit.entity.Player;

/**
 * The hunger event, changing the hunger of a player.
 */
public class HungerEvent implements OnlineEvent {
    /**
     * The hunger type, how the amount will be applied to the players hunger.
     */
    private final Variable<Hunger> hunger;

    /**
     * The amount of hunger to apply.
     */
    private final Variable<Number> amount;

    /**
     * Create the hunger event to set the given state.
     *
     * @param hunger the hunger type
     * @param amount the amount of hunger to apply
     */
    public HungerEvent(final Variable<Hunger> hunger, final Variable<Number> amount) {
        this.hunger = hunger;
        this.amount = amount;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        final Player player = profile.getPlayer();
        player.setFoodLevel(hunger.getValue(profile).calculate(player, amount.getValue(profile).intValue()));
    }
}
