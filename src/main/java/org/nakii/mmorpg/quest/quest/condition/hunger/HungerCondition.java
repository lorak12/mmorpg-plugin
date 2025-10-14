package org.nakii.mmorpg.quest.quest.condition.hunger;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;

/**
 * A condition that checks if the player's hunger level is at a certain level.
 */
public class HungerCondition implements OnlineCondition {

    /**
     * The hunger level required to pass the condition.
     */
    private final Variable<Number> hunger;

    /**
     * Create a new hunger condition.
     *
     * @param hunger the hunger level required to pass the condition
     */
    public HungerCondition(final Variable<Number> hunger) {
        this.hunger = hunger;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        return profile.getPlayer().getFoodLevel() >= hunger.getValue(profile).doubleValue();
    }
}
