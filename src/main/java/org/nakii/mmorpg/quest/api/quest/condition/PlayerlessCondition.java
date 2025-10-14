package org.nakii.mmorpg.quest.api.quest.condition;

import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Interface for playerless quest-conditions.
 * It represents the playerless condition as described in the BetonQuest user documentation.
 * For the player condition variant see {@link PlayerCondition}.
 */
@FunctionalInterface
public interface PlayerlessCondition {
    /**
     * Checks the condition.
     *
     * @return if the condition is fulfilled
     * @throws QuestException when the condition check fails
     */
    boolean check() throws QuestException;
}
