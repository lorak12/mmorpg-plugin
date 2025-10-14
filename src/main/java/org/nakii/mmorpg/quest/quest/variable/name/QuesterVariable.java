package org.nakii.mmorpg.quest.quest.variable.name;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.conversation.Conversation;

/**
 * This variable resolves into the name of the Npc in the conversation.
 */
public class QuesterVariable implements PlayerVariable {

    /**
     * Create a NpcName variable.
     */
    public QuesterVariable() {
    }

    @Override
    public String getValue(final Profile profile) throws QuestException {
        final Conversation conv = Conversation.getConversation(profile);
        if (conv == null) {
            return "";
        }
        return LegacyComponentSerializer.legacySection().serialize(conv.getData().getPublicData().quester().asComponent(profile));
    }
}
