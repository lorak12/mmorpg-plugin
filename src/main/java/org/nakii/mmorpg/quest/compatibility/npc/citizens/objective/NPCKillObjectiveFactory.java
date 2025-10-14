package org.nakii.mmorpg.quest.compatibility.npc.citizens.objective;

import net.citizensnpcs.api.npc.NPCRegistry;
import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.nakii.mmorpg.quest.compatibility.npc.citizens.CitizensArgument;

/**
 * Factory for creating {@link NPCKillObjective} instances from {@link Instruction}s.
 */
public class NPCKillObjectiveFactory implements ObjectiveFactory {

    /**
     * Source Registry of NPCs to use.
     */
    private final NPCRegistry registry;

    /**
     * Creates a new instance of the NPCKillObjectiveFactory.
     *
     * @param registry the registry of NPCs to use
     */
    public NPCKillObjectiveFactory(final NPCRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<NpcID> npcID = instruction.get(CitizensArgument.CITIZENS_ID);
        final Variable<Number> targetAmount = instruction.getValue("amount", Argument.NUMBER_NOT_LESS_THAN_ONE, 1);
        return new NPCKillObjective(instruction, registry, targetAmount, npcID);
    }
}
