package org.nakii.mmorpg.quest.quest.condition.experience;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;

/**
 * Requires the player to have specified level of experience or more.
 */
public class ExperienceCondition implements OnlineCondition {

    /**
     * The experience level the player needs to get.
     * The decimal part of the number is a percentage of the next level.
     */
    private final Variable<Number> amount;

    /**
     * Creates a new experience condition.
     *
     * @param amount The experience level the player needs to get.
     */
    public ExperienceCondition(final Variable<Number> amount) {
        this.amount = amount;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        final double amount = this.amount.getValue(profile).doubleValue();
        return profile.getPlayer().getLevel() + profile.getPlayer().getExp() >= amount;
    }
}
