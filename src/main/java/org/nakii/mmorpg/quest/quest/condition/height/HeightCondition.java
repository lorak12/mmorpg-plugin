package org.nakii.mmorpg.quest.quest.condition.height;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineCondition;

/**
 * Condition to check if a player is at a certain height or lower.
 */
public class HeightCondition implements OnlineCondition {

    /**
     * The height to check for.
     */
    private final Variable<Number> height;

    /**
     * Creates a new height condition.
     *
     * @param height the height to check for
     */
    public HeightCondition(final Variable<Number> height) {
        this.height = height;
    }

    @Override
    public boolean check(final OnlineProfile profile) throws QuestException {
        return profile.getPlayer().getLocation().getY() < height.getValue(profile).doubleValue();
    }
}
