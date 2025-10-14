package org.nakii.mmorpg.quest.api.quest.npc;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.kernel.TypeFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Factory to create specific {@link Npc}s from {@link Instruction}s.
 */
@FunctionalInterface
public interface NpcFactory extends TypeFactory<NpcWrapper<?>> {
    /**
     * Parses an instruction to create a {@link NpcWrapper} which resolves into a {@link Npc}.
     *
     * @param instruction instruction to parse
     * @return npc referenced by the instruction
     * @throws QuestException when the instruction cannot be parsed
     */
    @Override
    NpcWrapper<?> parseInstruction(Instruction instruction) throws QuestException;
}
