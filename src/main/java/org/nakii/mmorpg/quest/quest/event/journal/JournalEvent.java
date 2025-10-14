package org.nakii.mmorpg.quest.quest.event.journal;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.database.PlayerData;
import org.nakii.mmorpg.quest.feature.journal.Journal;
import org.nakii.mmorpg.quest.quest.event.NotificationSender;

/**
 * The journal event, doing what was defined in its instruction.
 */
public class JournalEvent implements PlayerEvent {

    /**
     * Storage used to get the {@link PlayerData}.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * Change to apply to a journal when the event is executed.
     */
    private final JournalChanger journalChanger;

    /**
     * Notification to send after the journal was changed.
     */
    private final NotificationSender notificationSender;

    /**
     * Create a journal event.
     *
     * @param dataStorage        to get player data
     * @param journalChanger     change to apply to a journal
     * @param notificationSender notification to send
     */
    public JournalEvent(final PlayerDataStorage dataStorage, final JournalChanger journalChanger, final NotificationSender notificationSender) {
        this.dataStorage = dataStorage;
        this.journalChanger = journalChanger;
        this.notificationSender = notificationSender;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        final PlayerData playerData = dataStorage.getOffline(profile);
        final Journal journal = playerData.getJournal();
        journalChanger.changeJournal(journal, profile);
        journal.update();
        notificationSender.sendNotification(profile);
    }
}
