package org.nakii.mmorpg.quest.quest.objective.variable;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

/**
 * Factory for creating {@link VariableObjective} instances from {@link Instruction}s.
 */
public class VariableObjectiveFactory implements ObjectiveFactory {
    /**
     * Creates a new VariableObjectiveFactory instance.
     */
    public VariableObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        if (instruction.hasArgument("no-chat")) {
            return new ChatVariableObjective(instruction);
        }
        return new VariableObjective(instruction);
    }
}
