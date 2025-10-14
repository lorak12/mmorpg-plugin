package org.nakii.mmorpg.quest.quest.event.journal;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEventFactory;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.database.Saver;
import org.nakii.mmorpg.quest.id.JournalEntryID;
import org.nakii.mmorpg.quest.quest.event.*;

import java.time.InstantSource;
import java.util.Locale;

/**
 * Factory to create journal events from {@link Instruction}s.
 */
public class JournalEventFactory implements PlayerEventFactory, PlayerlessEventFactory {
    /**
     * Logger factory to create a logger for the events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * BetonQuest instance to provide to events.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * The instant source to provide to events.
     */
    private final InstantSource instantSource;

    /**
     * The saver to inject into database-using events.
     */
    private final Saver saver;

    /**
     * The current active profile provider instance.
     */
    private final ProfileProvider profileProvider;

    /**
     * Create the journal event factory.
     *
     * @param loggerFactory   the logger factory to create a logger for the events
     * @param pluginMessage   the {@link PluginMessage} instance
     * @param dataStorage     storage for used player data
     * @param instantSource   instant source to pass on
     * @param saver           database saver to use
     * @param profileProvider the profile provider
     */
    public JournalEventFactory(final BetonQuestLoggerFactory loggerFactory, final PluginMessage pluginMessage, final PlayerDataStorage dataStorage, final InstantSource instantSource, final Saver saver, final ProfileProvider profileProvider) {
        this.loggerFactory = loggerFactory;
        this.pluginMessage = pluginMessage;
        this.dataStorage = dataStorage;
        this.instantSource = instantSource;
        this.saver = saver;
        this.profileProvider = profileProvider;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final String action = instruction.next();
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "update" -> createJournalUpdateEvent();
            case "add" -> createJournalAddEvent(instruction);
            case "delete" -> createJournalDeleteEvent(instruction);
            default -> throw new QuestException("Unknown journal action: " + action);
        };
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        final String action = instruction.next();
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "update", "add" -> new DoNothingPlayerlessEvent();
            case "delete" -> createStaticJournalDeleteEvent(instruction);
            default -> throw new QuestException("Unknown journal action: " + action);
        };
    }

    private JournalEvent createJournalDeleteEvent(final Instruction instruction) throws QuestException {
        final Variable<JournalEntryID> entryID = instruction.get(instruction.getPart(2), JournalEntryID::new);
        final JournalChanger journalChanger = new RemoveEntryJournalChanger(entryID);
        final NotificationSender notificationSender = new NoNotificationSender();
        return new JournalEvent(dataStorage, journalChanger, notificationSender);
    }

    private JournalEvent createJournalAddEvent(final Instruction instruction) throws QuestException {
        final Variable<JournalEntryID> entryID = instruction.get(instruction.getPart(2), JournalEntryID::new);
        final JournalChanger journalChanger = new AddEntryJournalChanger(instantSource, entryID);
        final NotificationSender notificationSender = new IngameNotificationSender(loggerFactory.create(JournalEvent.class),
                pluginMessage, instruction.getPackage(), instruction.getID().getFull(), NotificationLevel.INFO, "new_journal_entry");
        return new JournalEvent(dataStorage, journalChanger, notificationSender);
    }

    private JournalEvent createJournalUpdateEvent() {
        final JournalChanger journalChanger = new NoActionJournalChanger();
        final NotificationSender notificationSender = new NoNotificationSender();
        return new JournalEvent(dataStorage, journalChanger, notificationSender);
    }

    private PlayerlessEvent createStaticJournalDeleteEvent(final Instruction instruction) throws QuestException {
        final Variable<JournalEntryID> entryID = instruction.get(instruction.getPart(2), JournalEntryID::new);
        return new DeleteJournalPlayerlessEvent(dataStorage, saver, profileProvider, entryID);
    }
}
