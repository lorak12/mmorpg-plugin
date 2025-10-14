package org.nakii.mmorpg.quest.api.quest.condition.nullable;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * Quest condition that can work both with and without a profile.
 */
@FunctionalInterface
public interface NullableCondition {
    /**
     * Check the condition with a nullable profile.
     *
     * @param profile profile or null
     * @return true if the condition holds; false otherwise
     * @throws QuestException if the condition cannot be checked correctly,
     *                        this might indicate that the profile cannot be null
     *                        in this specific case
     */
    boolean check(@Nullable Profile profile) throws QuestException;
}
