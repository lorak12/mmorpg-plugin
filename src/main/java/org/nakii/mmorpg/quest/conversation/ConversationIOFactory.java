package org.nakii.mmorpg.quest.conversation;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * Factory to create Conversation IO for a conversation and online profile.
 */
@FunctionalInterface
public interface ConversationIOFactory {
    /**
     * Create the Conversation IO.
     *
     * @param conversation  the conversation to display
     * @param onlineProfile the player to show the conversation
     * @return the created conversation IO
     * @throws QuestException when the creation fails
     */
    ConversationIO parse(Conversation conversation, OnlineProfile onlineProfile) throws QuestException;
}
