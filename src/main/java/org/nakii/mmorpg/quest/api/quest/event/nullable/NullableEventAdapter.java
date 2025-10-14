package org.nakii.mmorpg.quest.api.quest.event.nullable;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;

/**
 * An adapter to handle both the {@link PlayerEvent} and {@link PlayerlessEvent} with one
 * common implementation of the {@link NullableEvent}.
 */
public final class NullableEventAdapter implements PlayerEvent, PlayerlessEvent {
    /**
     * Common null-safe event implementation.
     */
    private final NullableEvent event;

    /**
     * Create an adapter that handles events via the given common implementation.
     *
     * @param event common null-safe event implementation
     */
    public NullableEventAdapter(final NullableEvent event) {
        this.event = event;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        event.execute(profile);
    }

    @Override
    public void execute() throws QuestException {
        event.execute(null);
    }
}
