package org.nakii.mmorpg.quest.quest.objective.kill;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

import java.util.List;

/**
 * Factory for creating {@link KillPlayerObjective} instances from {@link Instruction}s.
 */
public class KillPlayerObjectiveFactory implements ObjectiveFactory {

    /**
     * Creates a new instance of the KillPlayerObjectiveFactory.
     */
    public KillPlayerObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Number> targetAmount = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ONE);
        final Variable<String> name = instruction.getValue("name", Argument.STRING);
        final Variable<List<ConditionID>> required = instruction.getValueList("required", ConditionID::new);
        return new KillPlayerObjective(instruction, targetAmount, name, required);
    }
}
