package org.nakii.mmorpg.quest.quest.variable.eval;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.nullable.NullableVariable;
import org.jetbrains.annotations.Nullable;

/**
 * A variable which evaluates to another variable.
 */
public class EvalVariable implements NullableVariable {
    /**
     * The original instruction.
     */
    private final Instruction instruction;

    /**
     * The evaluation input.
     */
    private final Variable<String> evaluation;

    /**
     * Create a new Eval variable.
     *
     * @param instruction the original instruction
     * @param evaluation  the evaluation input
     */
    public EvalVariable(final Instruction instruction, final Variable<String> evaluation) {
        this.instruction = instruction;
        this.evaluation = evaluation;
    }

    @Override
    public String getValue(@Nullable final Profile profile) throws QuestException {
        return instruction.get("%" + evaluation.getValue(profile) + "%", Argument.STRING).getValue(profile);
    }
}
