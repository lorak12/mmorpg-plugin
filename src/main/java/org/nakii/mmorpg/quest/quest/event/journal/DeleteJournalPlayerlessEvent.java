package org.nakii.mmorpg.quest.quest.event.journal;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.database.PlayerData;
import org.nakii.mmorpg.quest.database.Saver;
import org.nakii.mmorpg.quest.database.UpdateType;
import org.nakii.mmorpg.quest.feature.journal.Journal;
import org.nakii.mmorpg.quest.id.JournalEntryID;

/**
 * Deletes the journal entry from all online players and database entries.
 */
public class DeleteJournalPlayerlessEvent implements PlayerlessEvent {
    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * Database saver to use for writing offline player data.
     */
    private final Saver saver;

    /**
     * The profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * Point category to remove.
     */
    private final Variable<JournalEntryID> entryID;

    /**
     * Create a new Journal remove event for every player, online and offline.
     *
     * @param dataStorage     the storage providing player data
     * @param saver           the saver to use
     * @param profileProvider the profile provider instance
     * @param entryID         the entry to remove
     */
    public DeleteJournalPlayerlessEvent(final PlayerDataStorage dataStorage, final Saver saver, final ProfileProvider profileProvider,
                                        final Variable<JournalEntryID> entryID) {
        this.dataStorage = dataStorage;
        this.saver = saver;
        this.profileProvider = profileProvider;
        this.entryID = entryID;
    }

    @Override
    public void execute() throws QuestException {
        final JournalEntryID resolved = this.entryID.getValue(null);
        for (final OnlineProfile profile : profileProvider.getOnlineProfiles()) {
            final PlayerData playerData = dataStorage.getOffline(profile);
            final Journal journal = playerData.getJournal();
            journal.removePointer(resolved);
            journal.update();
        }
        saver.add(new Saver.Record(UpdateType.REMOVE_ALL_ENTRIES, resolved.getFull()));
    }
}
