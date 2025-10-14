package org.nakii.mmorpg.quest.compatibility.npc.fancynpcs;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcManager;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.NpcFactory;
import org.nakii.mmorpg.quest.api.quest.npc.NpcWrapper;

/**
 * Factory to get FancyNpcs Npcs.
 */
public class FancyFactory implements NpcFactory {
    /**
     * The empty default constructor.
     */
    public FancyFactory() {
    }

    @Override
    public NpcWrapper<Npc> parseInstruction(final Instruction instruction) throws QuestException {
        final NpcManager npcManager = FancyNpcsPlugin.get().getNpcManager();
        return new FancyWrapper(npcManager, instruction.get(Argument.STRING), instruction.hasArgument("byName"));
    }
}
