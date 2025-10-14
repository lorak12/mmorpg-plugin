package org.nakii.mmorpg.quest.api.quest;

import org.nakii.mmorpg.quest.api.instruction.Instruction;

/**
 * Factory to create a specific {@link T}.
 * <p>
 * Opposed to the {@link PlayerQuestFactory} it is used without a
 * {@link org.nakii.mmorpg.quest.api.profile.Profile Profile}.
 *
 * @param <T> quest type executed without a player
 */
@FunctionalInterface
public interface PlayerlessQuestFactory<T> {
    /**
     * Parses an instruction to create a {@link T}.
     *
     * @param instruction instruction to parse
     * @return {@link T} represented by the instruction
     * @throws QuestException when the instruction cannot be parsed
     */
    T parsePlayerless(Instruction instruction) throws QuestException;
}
