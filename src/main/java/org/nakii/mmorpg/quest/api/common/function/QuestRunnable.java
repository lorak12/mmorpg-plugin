package org.nakii.mmorpg.quest.api.common.function;

import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * A simple {@link java.lang.Runnable} that can throw a {@link QuestException}.
 */
@FunctionalInterface
public interface QuestRunnable {
    /**
     * Executes the runnable.
     *
     * @throws QuestException if the runnable fails
     */
    void run() throws QuestException;
}
