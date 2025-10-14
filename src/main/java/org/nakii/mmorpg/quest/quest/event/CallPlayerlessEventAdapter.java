package org.nakii.mmorpg.quest.quest.event;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;

/**
 * Adapter to allow executing a playerless event with the API of a player event.
 */
public class CallPlayerlessEventAdapter implements PlayerEvent {

    /**
     * The playerless event to execute.
     */
    private final PlayerlessEvent event;

    /**
     * Create a player event that will execute the given playerless event.
     *
     * @param event playerless event to execute
     */
    public CallPlayerlessEventAdapter(final PlayerlessEvent event) {
        this.event = event;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        event.execute();
    }
}
