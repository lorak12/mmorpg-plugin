package org.nakii.mmorpg.quest.quest.condition.number;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableConditionAdapter;

/**
 * The condition factory for the number compare condition.
 */
public class NumberCompareConditionFactory implements PlayerConditionFactory, PlayerlessConditionFactory {

    /**
     * Creates the number compare condition factory.
     */
    public NumberCompareConditionFactory() {
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        return new NullableConditionAdapter(parse(instruction));
    }

    @Override
    public PlayerlessCondition parsePlayerless(final Instruction instruction) throws QuestException {
        return new NullableConditionAdapter(parse(instruction));
    }

    private NumberCompareCondition parse(final Instruction instruction) throws QuestException {
        final Variable<Number> first = instruction.get(Argument.NUMBER);
        final Operation operation = Operation.fromSymbol(instruction.next());
        final Variable<Number> second = instruction.get(Argument.NUMBER);
        return new NumberCompareCondition(first, second, operation);
    }
}
