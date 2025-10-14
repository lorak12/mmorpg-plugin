package org.nakii.mmorpg.quest.api.quest.variable;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.PlayerQuestFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Factory to create a specific {@link PlayerVariable} from {@link Instruction}s.
 */
@FunctionalInterface
public interface PlayerVariableFactory extends PlayerQuestFactory<PlayerVariable> {
    /**
     * Parses an instruction to create a {@link PlayerVariable}.
     *
     * @param instruction instruction to parse
     * @return variable represented by the instruction
     * @throws QuestException when the instruction cannot be parsed
     */
    @Override
    PlayerVariable parsePlayer(Instruction instruction) throws QuestException;
}
