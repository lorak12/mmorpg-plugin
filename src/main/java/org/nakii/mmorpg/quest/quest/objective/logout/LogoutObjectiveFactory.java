package org.nakii.mmorpg.quest.quest.objective.logout;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

/**
 * Factory for creating {@link LogoutObjective} instances from {@link Instruction}s.
 */
public class LogoutObjectiveFactory implements ObjectiveFactory {
    /**
     * Creates a new instance of the LogoutObjectiveFactory.
     */
    public LogoutObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        return new LogoutObjective(instruction);
    }
}
