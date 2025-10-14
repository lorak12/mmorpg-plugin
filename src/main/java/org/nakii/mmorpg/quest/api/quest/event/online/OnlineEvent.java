package org.nakii.mmorpg.quest.api.quest.event.online;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Quest event that needs an online profile to function correctly.
 */
@FunctionalInterface
public interface OnlineEvent {
    /**
     * Execute the event with an online profile.
     *
     * @param profile online profile to run the event with
     * @throws QuestException if the execution of the event fails
     */
    void execute(OnlineProfile profile) throws QuestException;
}
