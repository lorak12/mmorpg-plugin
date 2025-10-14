package org.nakii.mmorpg.quest.conversation.io;

import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.conversation.ConversationColors;
import org.nakii.mmorpg.quest.conversation.ConversationIO;
import org.nakii.mmorpg.quest.conversation.ConversationIOFactory;

/**
 * Tellraw conversation output.
 */
public class TellrawConvIOFactory implements ConversationIOFactory {
    /**
     * The colors used for the conversation.
     */
    private final ConversationColors colors;

    /**
     * Create a new Tellraw conversation IO factory.
     *
     * @param colors the colors used for the conversation
     */
    public TellrawConvIOFactory(final ConversationColors colors) {
        this.colors = colors;
    }

    @Override
    public ConversationIO parse(final Conversation conversation, final OnlineProfile onlineProfile) {
        return new TellrawConvIO(conversation, onlineProfile, colors);
    }
}
