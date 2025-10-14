package org.nakii.mmorpg.quest.compatibility.npc.citizens;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.NpcFactory;
import org.nakii.mmorpg.quest.api.quest.npc.NpcWrapper;

/**
 * Creates validated Npc Wrapper for Citizens Npcs.
 */
public class CitizensNpcFactory implements NpcFactory {
    /**
     * Source Registry of NPCs to use.
     */
    private final NPCRegistry registry;

    /**
     * Create a new Npc Factory with a specific registry.
     *
     * @param registry the registry of NPCs to use
     */
    public CitizensNpcFactory(final NPCRegistry registry) {
        this.registry = registry;
    }

    @Override
    public NpcWrapper<NPC> parseInstruction(final Instruction instruction) throws QuestException {
        if (instruction.hasArgument("byName")) {
            return new CitizensNameWrapper(registry, instruction.get(Argument.STRING));
        }
        final Variable<Number> npcId = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ZERO);
        return new CitizensWrapper(registry, npcId);
    }
}
