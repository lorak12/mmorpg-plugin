package org.nakii.mmorpg.quest.quest.event.run;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEvent;
import org.nakii.mmorpg.quest.kernel.processor.adapter.EventAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Allows for running multiple events.
 */
public class RunEvent implements NullableEvent {

    /**
     * Events that the run event will execute.
     */
    private final List<EventAdapter> events;

    /**
     * Create a run event from the given instruction.
     *
     * @param events events to run
     */
    public RunEvent(final List<EventAdapter> events) {
        this.events = events;
    }

    @Override
    public void execute(@Nullable final Profile profile) throws QuestException {
        for (final EventAdapter event : events) {
            event.fire(profile);
        }
    }
}
