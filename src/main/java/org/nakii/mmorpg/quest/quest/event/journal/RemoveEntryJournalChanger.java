package org.nakii.mmorpg.quest.quest.event.journal;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.feature.journal.Journal;
import org.nakii.mmorpg.quest.id.JournalEntryID;

/**
 * A journal changer that will remove a specified entry.
 */
public class RemoveEntryJournalChanger implements JournalChanger {

    /**
     * Entry to remove from the journal.
     */
    private final Variable<JournalEntryID> entryID;

    /**
     * Create the entry-removing journal changer.
     *
     * @param entryID entry to remove
     */
    public RemoveEntryJournalChanger(final Variable<JournalEntryID> entryID) {
        this.entryID = entryID;
    }

    @Override
    public void changeJournal(final Journal journal, final Profile profile) throws QuestException {
        journal.removePointer(entryID.getValue(profile));
    }
}
