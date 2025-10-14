package org.nakii.mmorpg.quest.quest.event.journal;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.feature.journal.Journal;
import org.nakii.mmorpg.quest.feature.journal.Pointer;
import org.nakii.mmorpg.quest.id.JournalEntryID;

import java.time.InstantSource;

/**
 * A journal changer that will add a specified entry.
 */
public class AddEntryJournalChanger implements JournalChanger {

    /**
     * Instant source for new journal entries.
     */
    private final InstantSource instantSource;

    /**
     * Entry to add to the journal.
     */
    private final Variable<JournalEntryID> entryID;

    /**
     * Create the entry-adding journal changer.
     *
     * @param instantSource source to get the journal entry date from
     * @param entryID       entry to add
     */
    public AddEntryJournalChanger(final InstantSource instantSource, final Variable<JournalEntryID> entryID) {
        this.instantSource = instantSource;
        this.entryID = entryID;
    }

    @Override
    public void changeJournal(final Journal journal, final Profile profile) throws QuestException {
        journal.addPointer(new Pointer(entryID.getValue(profile), instantSource.millis()));
    }
}
