package org.nakii.mmorpg.quest.quest.objective.npc;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.nakii.mmorpg.quest.quest.objective.interact.Interaction;

import static org.nakii.mmorpg.quest.quest.objective.interact.Interaction.RIGHT;

/**
 * Factory for creating {@link NpcInteractObjective} instances from {@link Instruction}s.
 */
public class NpcInteractObjectiveFactory implements ObjectiveFactory {

    /**
     * Creates a new instance of the NpcInteractObjectiveFactory.
     */
    public NpcInteractObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<NpcID> npcId = instruction.get(NpcID::new);
        final boolean cancel = instruction.hasArgument("cancel");
        final Variable<Interaction> interactionType = instruction.getValue("interaction", Argument.ENUM(Interaction.class), RIGHT);
        return new NpcInteractObjective(instruction, npcId, cancel, interactionType);
    }
}
