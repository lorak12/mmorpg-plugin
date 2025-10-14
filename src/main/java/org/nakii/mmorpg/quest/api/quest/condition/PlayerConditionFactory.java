package org.nakii.mmorpg.quest.api.quest.condition;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.PlayerQuestFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Factory to create a specific {@link PlayerCondition} from {@link Instruction}s.
 */
@FunctionalInterface
public interface PlayerConditionFactory extends PlayerQuestFactory<PlayerCondition> {
    /**
     * Parses an instruction to create a {@link PlayerCondition}.
     *
     * @param instruction instruction to parse
     * @return condition represented by the instruction
     * @throws QuestException when the instruction cannot be parsed
     */
    @Override
    PlayerCondition parsePlayer(Instruction instruction) throws QuestException;
}
