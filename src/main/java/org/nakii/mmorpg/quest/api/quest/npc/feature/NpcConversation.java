package org.nakii.mmorpg.quest.api.quest.npc.feature;

import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.npc.Npc;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.conversation.ConversationID;
import org.bukkit.Location;

/**
 * Represents a conversation with Npc.
 *
 * @param <T> the original npc type
 */
public class NpcConversation<T> extends Conversation {
    /**
     * Npc used in this Conversation.
     */
    private final Npc<T> npc;

    /**
     * Starts a new conversation between player and npc at given location.
     *
     * @param log            the logger that will be used for logging
     * @param pluginMessage  the {@link PluginMessage} instance
     * @param onlineProfile  the profile of the player
     * @param conversationID ID of the conversation
     * @param center         location where the conversation has been started
     * @param npc            the Npc used for this conversation
     */
    public NpcConversation(final BetonQuestLogger log, final PluginMessage pluginMessage, final OnlineProfile onlineProfile, final ConversationID conversationID,
                           final Location center, final Npc<T> npc) {
        super(log, pluginMessage, onlineProfile, conversationID, center);
        this.npc = npc;
    }

    /**
     * This will return the Npc associated with this conversation.
     *
     * @return the BetonQuest Npc
     */
    public Npc<T> getNPC() {
        return npc;
    }
}
