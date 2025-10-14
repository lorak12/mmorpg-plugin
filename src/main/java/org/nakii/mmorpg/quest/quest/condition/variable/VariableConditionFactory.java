package org.nakii.mmorpg.quest.quest.condition.variable;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableConditionAdapter;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerlessCondition;

/**
 * Factory for {@link VariableCondition}s.
 */
public class VariableConditionFactory implements PlayerConditionFactory, PlayerlessConditionFactory {

    /**
     * Logger factory to create a logger for the conditions.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Data used for primary server access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Creates a new factory for {@link VariableCondition}s.
     *
     * @param loggerFactory the logger factory to create a logger for the conditions
     * @param data          the data used for primary server access
     */
    public VariableConditionFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data) {
        this.loggerFactory = loggerFactory;
        this.data = data;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final NullableConditionAdapter condition = new NullableConditionAdapter(parse(instruction));
        if (instruction.hasArgument("forceSync")) {
            return new PrimaryServerThreadPlayerCondition(condition, data);
        }
        return condition;
    }

    @Override
    public PlayerlessCondition parsePlayerless(final Instruction instruction) throws QuestException {
        final NullableConditionAdapter condition = new NullableConditionAdapter(parse(instruction));
        if (instruction.hasArgument("forceSync")) {
            return new PrimaryServerThreadPlayerlessCondition(condition, data);
        }
        return condition;
    }

    private VariableCondition parse(final Instruction instruction) throws QuestException {
        final Variable<String> variable = instruction.get(Argument.STRING);
        final Variable<String> regex = instruction.get(Argument.STRING);
        final String variableAddress = instruction.getID().toString();
        final BetonQuestLogger log = loggerFactory.create(VariableCondition.class);
        return new VariableCondition(log, variable, regex, variableAddress);
    }
}
