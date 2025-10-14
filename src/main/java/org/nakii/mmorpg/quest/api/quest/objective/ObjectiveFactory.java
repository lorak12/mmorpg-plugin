package org.nakii.mmorpg.quest.api.quest.objective;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.kernel.TypeFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Factory to create a specific {@link Objective} from {@link Instruction}s.
 */
@FunctionalInterface
public interface ObjectiveFactory extends TypeFactory<Objective> {
    /**
     * Parses an instruction to create a {@link Objective}.
     *
     * @param instruction instruction to parse
     * @return objective referenced by the instruction
     * @throws QuestException when the instruction cannot be parsed
     */
    @Override
    Objective parseInstruction(Instruction instruction) throws QuestException;
}
