package org.nakii.mmorpg.quest.quest.event.journal;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.feature.journal.Journal;

/**
 * Defines changes to be done to a journal.
 */
@FunctionalInterface
public interface JournalChanger {
    /**
     * Apply the change to a journal.
     *
     * @param journal journal to change
     * @param profile the profile to resolve variables for
     * @throws QuestException when an exception occurs
     */
    void changeJournal(Journal journal, Profile profile) throws QuestException;
}
