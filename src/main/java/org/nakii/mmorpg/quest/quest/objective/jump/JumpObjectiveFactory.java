package org.nakii.mmorpg.quest.quest.objective.jump;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

/**
 * Factory for creating {@link JumpObjective} instances from {@link Instruction}s.
 */
public class JumpObjectiveFactory implements ObjectiveFactory {
    /**
     * Creates a new instance of the JumpObjectiveFactory.
     */
    public JumpObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Number> targetAmount = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ONE);
        return new JumpObjective(instruction, targetAmount);
    }
}
