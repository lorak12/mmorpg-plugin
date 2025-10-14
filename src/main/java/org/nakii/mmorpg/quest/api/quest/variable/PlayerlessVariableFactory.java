package org.nakii.mmorpg.quest.api.quest.variable;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.PlayerlessQuestFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Factory to create a specific {@link PlayerlessVariable} from {@link Instruction}s.
 */
@FunctionalInterface
public interface PlayerlessVariableFactory extends PlayerlessQuestFactory<PlayerlessVariable> {
    /**
     * Parses an instruction to create a {@link PlayerlessVariable}.
     *
     * @param instruction instruction to parse
     * @return variable represented by the instruction
     * @throws QuestException when the instruction cannot be parsed
     */
    @Override
    PlayerlessVariable parsePlayerless(Instruction instruction) throws QuestException;
}
