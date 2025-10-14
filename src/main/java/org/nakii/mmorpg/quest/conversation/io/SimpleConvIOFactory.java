package org.nakii.mmorpg.quest.conversation.io;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.conversation.ConversationColors;
import org.nakii.mmorpg.quest.conversation.ConversationIO;
import org.nakii.mmorpg.quest.conversation.ConversationIOFactory;

/**
 * Simple chat-based conversation output.
 */
public class SimpleConvIOFactory implements ConversationIOFactory {
    /**
     * The colors used for the conversation.
     */
    private final ConversationColors colors;

    /**
     * Create a new Simple conversation IO factory.
     *
     * @param colors the colors used for the conversation
     */
    public SimpleConvIOFactory(final ConversationColors colors) {
        this.colors = colors;
    }

    @Override
    public ConversationIO parse(final Conversation conversation, final OnlineProfile onlineProfile) {
        return new SimpleConvIO(conversation, onlineProfile, colors);
    }
}
