package org.nakii.mmorpg.quest.quest.condition.hunger;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineConditionAdapter;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition;

/**
 * Factory for {@link HungerCondition}s.
 */
public class HungerConditionFactory implements PlayerConditionFactory {

    /**
     * Logger factory to create a logger for the conditions.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Data used for condition check on the primary server thread.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create the hunger factory.
     *
     * @param loggerFactory the logger factory to create a logger for the conditions
     * @param data          the data used for checking the condition on the main thread
     */
    public HungerConditionFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data) {
        this.loggerFactory = loggerFactory;
        this.data = data;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<Number> hunger = instruction.get(Argument.NUMBER);
        final BetonQuestLogger log = loggerFactory.create(HungerCondition.class);
        return new PrimaryServerThreadPlayerCondition(
                new OnlineConditionAdapter(new HungerCondition(hunger), log, instruction.getPackage()), data);
    }
}
