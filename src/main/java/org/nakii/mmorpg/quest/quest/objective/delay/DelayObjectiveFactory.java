package org.nakii.mmorpg.quest.quest.objective.delay;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

/**
 * Factory for creating {@link DelayObjective} instances from {@link Instruction}s.
 */
public class DelayObjectiveFactory implements ObjectiveFactory {

    /**
     * Creates a new instance of the DelayObjectiveFactory.
     */
    public DelayObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Number> delay = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ZERO);
        final Variable<Number> interval = instruction.getValue("interval", Argument.NUMBER_NOT_LESS_THAN_ONE, 20 * 10);
        return new DelayObjective(instruction, interval, delay);
    }
}
