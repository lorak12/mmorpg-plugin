package org.nakii.mmorpg.quest.quest.condition;

import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;

/**
 * A playerless condition placeholder that throws an exception when checked.
 */
public class ThrowExceptionPlayerlessCondition implements PlayerlessCondition {

    /**
     * Create a playerless condition that throws an exception when checked.
     */
    public ThrowExceptionPlayerlessCondition() {
    }

    @Override
    public boolean check() throws QuestException {
        throw new QuestException("This condition cannot be checked in the current context.");
    }
}
