package org.nakii.mmorpg.quest.api.quest.variable.online;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Player Variable that needs an online profile to function correctly.
 */
@FunctionalInterface
public interface OnlineVariable {
    /**
     * Gets the resolved value for given profile.
     *
     * @param profile the {@link OnlineProfile} to get the value for
     * @return the value of this variable
     * @throws QuestException when the value could not be retrieved
     */
    String getValue(OnlineProfile profile) throws QuestException;
}
