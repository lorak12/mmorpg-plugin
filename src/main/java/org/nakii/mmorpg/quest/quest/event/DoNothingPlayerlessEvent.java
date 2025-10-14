package org.nakii.mmorpg.quest.quest.event;

import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;

/**
 * Static event that does nothing. It can be used as a placeholder when static event execution isn't an error in itself
 * or in the case that explicitly nothing should happen.
 */
public class DoNothingPlayerlessEvent implements PlayerlessEvent {
    /**
     * Create a static event placeholder that doesn't do anything.
     */
    public DoNothingPlayerlessEvent() {
    }

    @Override
    public void execute() {
        // null object pattern
    }
}
