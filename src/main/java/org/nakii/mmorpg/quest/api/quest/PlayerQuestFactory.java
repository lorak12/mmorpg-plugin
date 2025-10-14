package org.nakii.mmorpg.quest.api.quest;

import org.nakii.mmorpg.quest.api.instruction.Instruction;

/**
 * Factory to create a specific {@link T}.
 *
 * @param <T> quest type requiring a player for execution
 */
@FunctionalInterface
public interface PlayerQuestFactory<T> {
    /**
     * Parses an instruction to create a {@link T}.
     *
     * @param instruction instruction to parse
     * @return {@link T} represented by the instruction
     * @throws QuestException when the instruction cannot be parsed
     */
    T parsePlayer(Instruction instruction) throws QuestException;
}
