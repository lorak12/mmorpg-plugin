package org.nakii.mmorpg.quest.compatibility.npc.citizens;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.IdentifierArgument;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;

/**
 * Parses a string to a Citizens Npc ID.
 */
public class CitizensArgument implements IdentifierArgument<NpcID> {
    /**
     * The default instance of {@link CitizensArgument}.
     */
    public static final CitizensArgument CITIZENS_ID = new CitizensArgument();

    /**
     * Creates a new parser for Citizens Npc Ids.
     */
    public CitizensArgument() {
    }

    @Override
    public NpcID apply(final QuestPackageManager packManager, final QuestPackage pack, final String string) throws QuestException {
        final NpcID npcId = new NpcID(packManager, pack, string);
        final Instruction npcInstruction = npcId.getInstruction();
        if (!"citizens".equals(npcInstruction.getPart(0))) {
            throw new QuestException("Cannot use non-Citizens NPC ID!");
        }
        return npcId;
    }
}
