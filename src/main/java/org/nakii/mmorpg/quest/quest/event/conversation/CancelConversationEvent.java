package org.nakii.mmorpg.quest.quest.event.conversation;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEvent;
import org.nakii.mmorpg.quest.conversation.Conversation;

/**
 * Cancels the conversation.
 */
public class CancelConversationEvent implements OnlineEvent {

    /**
     * Create a new conversation cancel event.
     */
    public CancelConversationEvent() {
    }

    @Override
    public void execute(final OnlineProfile profile) {
        final Conversation conv = Conversation.getConversation(profile);
        if (conv != null) {
            conv.endConversation();
        }
    }
}
