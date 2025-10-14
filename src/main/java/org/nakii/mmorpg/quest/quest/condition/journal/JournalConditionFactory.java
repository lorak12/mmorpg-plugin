package org.nakii.mmorpg.quest.quest.condition.journal;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineConditionAdapter;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.id.JournalEntryID;

/**
 * Factory for {@link JournalCondition}s.
 */
public class JournalConditionFactory implements PlayerConditionFactory {

    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * Logger factory to create a logger for the conditions.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Create the journal condition factory.
     *
     * @param dataStorage   the storage providing player data
     * @param loggerFactory the logger factory to create a logger for the conditions
     */
    public JournalConditionFactory(final PlayerDataStorage dataStorage, final BetonQuestLoggerFactory loggerFactory) {
        this.dataStorage = dataStorage;
        this.loggerFactory = loggerFactory;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<JournalEntryID> entryID = instruction.get(JournalEntryID::new);
        final BetonQuestLogger log = loggerFactory.create(JournalCondition.class);
        return new OnlineConditionAdapter(new JournalCondition(dataStorage, entryID), log, instruction.getPackage());
    }
}
