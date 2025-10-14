package org.nakii.mmorpg.quest.api.kernel;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * A factory to create a Quest Type from an {@link Instruction}.
 *
 * @param <T> the type to create
 */
@FunctionalInterface
public interface TypeFactory<T> {
    /**
     * Create a new {@link T} from an {@link Instruction}.
     *
     * @param instruction the instruction to parse
     * @return the newly created {@link T}
     * @throws QuestException if the instruction cannot be parsed
     */
    T parseInstruction(Instruction instruction) throws QuestException;
}
