package org.nakii.mmorpg.quest.engine.api;

import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.instruction.Instruction;

@FunctionalInterface
public interface ObjectiveFactory {
    Objective create(Instruction instruction) throws QuestException;
}