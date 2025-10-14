package org.nakii.mmorpg.quest.api.quest.event;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.PlayerlessQuestFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Factory to create a specific {@link PlayerlessEvent} from {@link Instruction}s.
 */
@FunctionalInterface
public interface PlayerlessEventFactory extends PlayerlessQuestFactory<PlayerlessEvent> {
    /**
     * Parses an instruction to create a {@link PlayerlessEvent}.
     *
     * @param instruction instruction to parse
     * @return event represented by the instruction
     * @throws QuestException when the instruction cannot be parsed
     */
    @Override
    PlayerlessEvent parsePlayerless(Instruction instruction) throws QuestException;
}
