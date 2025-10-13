package org.nakii.mmorpg.quest.engine.api;

import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.instruction.Instruction;

@FunctionalInterface
public interface EventFactory {
    QuestEvent create(Instruction instruction) throws QuestException;
}