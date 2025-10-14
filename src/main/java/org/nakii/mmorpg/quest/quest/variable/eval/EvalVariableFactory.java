package org.nakii.mmorpg.quest.quest.variable.eval;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariableFactory;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariableFactory;
import org.nakii.mmorpg.quest.api.quest.variable.nullable.NullableVariableAdapter;

/**
 * A factory for creating Eval variables.
 */
public class EvalVariableFactory implements PlayerVariableFactory, PlayerlessVariableFactory {

    /**
     * Create a new Eval variable factory.
     */
    public EvalVariableFactory() {
    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) throws QuestException {
        return parseEvalVariable(instruction);
    }

    @Override
    public PlayerlessVariable parsePlayerless(final Instruction instruction) throws QuestException {
        return parseEvalVariable(instruction);
    }

    private NullableVariableAdapter parseEvalVariable(final Instruction instruction) throws QuestException {
        final String rawInstruction = String.join(".", instruction.getValueParts());
        return new NullableVariableAdapter(new EvalVariable(
                instruction, instruction.get(rawInstruction, Argument.STRING)
        ));
    }
}
