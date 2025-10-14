package org.nakii.mmorpg.quest.quest.event.notify;

import org.nakii.mmorpg.quest.api.LanguageProvider;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEventFactory;
import org.nakii.mmorpg.quest.api.text.TextParser;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.quest.event.CallPlayerlessEventAdapter;
import org.nakii.mmorpg.quest.quest.event.OnlineProfileGroupPlayerlessEventAdapter;

/**
 * Factory for the notify all event.
 */
public class NotifyAllEventFactory extends NotifyEventFactory implements PlayerEventFactory, PlayerlessEventFactory {
    /**
     * The profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * Creates the notify all event factory.
     *
     * @param loggerFactory    the logger factory to create a logger for the events
     * @param data             the data for primary server thread access
     * @param textParser       the text parser to use for parsing text
     * @param dataStorage      the storage providing player data
     * @param profileProvider  the profile provider instance
     * @param languageProvider the language provider to get the default language
     */
    public NotifyAllEventFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data,
                                 final TextParser textParser, final PlayerDataStorage dataStorage,
                                 final ProfileProvider profileProvider, final LanguageProvider languageProvider) {
        super(loggerFactory, data, textParser, dataStorage, languageProvider);
        this.profileProvider = profileProvider;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return new CallPlayerlessEventAdapter(parsePlayerless(instruction));
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return new OnlineProfileGroupPlayerlessEventAdapter(profileProvider::getOnlineProfiles, super.parsePlayer(instruction));
    }
}
