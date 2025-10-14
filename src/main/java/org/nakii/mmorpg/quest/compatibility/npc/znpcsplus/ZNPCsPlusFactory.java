package org.nakii.mmorpg.quest.compatibility.npc.znpcsplus;

import lol.pyr.znpcsplus.api.npc.NpcEntry;
import lol.pyr.znpcsplus.api.npc.NpcRegistry;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.NpcFactory;
import org.nakii.mmorpg.quest.api.quest.npc.NpcWrapper;

/**
 * Factory to get ZNPCsPlus Npcs.
 */
public class ZNPCsPlusFactory implements NpcFactory {

    /**
     * ZNPCsPlus Npc Registry.
     */
    private final NpcRegistry npcRegistry;

    /**
     * The empty default constructor.
     *
     * @param npcRegistry the Npc Registry to get Npcs from
     */
    public ZNPCsPlusFactory(final NpcRegistry npcRegistry) {
        this.npcRegistry = npcRegistry;
    }

    @Override
    public NpcWrapper<NpcEntry> parseInstruction(final Instruction instruction) throws QuestException {
        return new ZNPCsPlusWrapper(npcRegistry, instruction.get(Argument.STRING));
    }
}
