package org.nakii.mmorpg.quest.api.quest.event.nullable;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * Quest event that can work both with and without a profile.
 */
@FunctionalInterface
public interface NullableEvent {
    /**
     * Execute the event with a nullable profile.
     *
     * @param profile profile or null
     * @throws QuestException if the event cannot be executed correctly,
     *                        this might indicate that the profile cannot be null
     *                        in this specific case
     */
    void execute(@Nullable Profile profile) throws QuestException;
}
