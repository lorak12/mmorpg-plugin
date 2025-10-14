package org.nakii.mmorpg.quest.api.quest.condition.online;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Quest condition that needs an online profile to function correctly.
 */
@FunctionalInterface
public interface OnlineCondition {
    /**
     * Checks the condition with an online profile.
     *
     * @param profile online profile to check the condition with
     * @return if the condition is fulfilled
     * @throws QuestException when the condition check fails
     */
    boolean check(OnlineProfile profile) throws QuestException;
}
