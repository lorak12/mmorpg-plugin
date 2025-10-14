package org.nakii.mmorpg.quest.quest.objective.login;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

/**
 * Factory for creating {@link LoginObjective} instances from {@link Instruction}s.
 */
public class LoginObjectiveFactory implements ObjectiveFactory {
    /**
     * Creates a new instance of the LoginObjectiveFactory.
     */
    public LoginObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        return new LoginObjective(instruction);
    }
}
