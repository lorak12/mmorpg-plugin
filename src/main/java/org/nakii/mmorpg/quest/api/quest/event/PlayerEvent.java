package org.nakii.mmorpg.quest.api.quest.event;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Interface for quest-events that are executed for a profile. It represents the player event as described in the
 * BetonQuest user documentation. It does not represent the playerless variant though, see {@link PlayerlessEvent}.
 */
@FunctionalInterface
public interface PlayerEvent {
    /**
     * Executes the event.
     *
     * @param profile the {@link Profile} the event is executed for
     * @throws QuestException when the event execution fails
     */
    void execute(Profile profile) throws QuestException;
}
