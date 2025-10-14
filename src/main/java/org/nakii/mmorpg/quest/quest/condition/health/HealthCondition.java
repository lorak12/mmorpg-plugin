package org.nakii.mmorpg.quest.quest.condition.health;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;

/**
 * Requires the player to have specified amount of health (or more).
 */
public class HealthCondition implements OnlineCondition {

    /**
     * The health value.
     */
    private final Variable<Number> health;

    /**
     * Creates a new health condition.
     *
     * @param health The health value
     */
    public HealthCondition(final Variable<Number> health) {
        this.health = health;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        final double expectedHealth = health.getValue(profile).doubleValue();
        return profile.getPlayer().getHealth() >= expectedHealth;
    }
}
