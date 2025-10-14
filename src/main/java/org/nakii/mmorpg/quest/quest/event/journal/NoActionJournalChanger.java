package org.nakii.mmorpg.quest.quest.event.journal;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.feature.journal.Journal;

/**
 * A journal changer that will not change anything.
 */
public class NoActionJournalChanger implements JournalChanger {
    /**
     * Create the no action journal changer.
     */
    public NoActionJournalChanger() {
    }

    @Override
    public void changeJournal(final Journal journal, final Profile profile) {
        // null object pattern
    }
}
