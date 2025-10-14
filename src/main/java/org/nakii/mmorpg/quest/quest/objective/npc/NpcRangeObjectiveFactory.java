package org.nakii.mmorpg.quest.quest.objective.npc;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

import java.util.List;

/**
 * Factory for creating {@link NpcRangeObjective} instances from {@link Instruction}s.
 */
public class NpcRangeObjectiveFactory implements ObjectiveFactory {
    /**
     * Creates a new instance of the NpcRangeObjectiveFactory.
     */
    public NpcRangeObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<List<NpcID>> npcIds = instruction.getList(NpcID::new);
        final Variable<Trigger> trigger = instruction.get(Argument.ENUM(Trigger.class));
        final Variable<Number> radius = instruction.get(Argument.NUMBER);
        return new NpcRangeObjective(instruction, npcIds, radius, trigger);
    }
}
