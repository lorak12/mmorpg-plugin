package org.nakii.mmorpg.quest.quest.condition.conversation;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.conversation.Conversation;
import org.nakii.mmorpg.quest.conversation.ConversationID;
import org.jetbrains.annotations.Nullable;

/**
 * Condition to check if a player is in a conversation or, if specified, in the specified conversation.
 */
public class InConversationCondition implements PlayerCondition {

    /**
     * Identifier of the conversation.
     */
    @Nullable
    private final Variable<ConversationID> conversationID;

    /**
     * Constructor of the InConversationCondition.
     *
     * @param conversationID the conversation identifier
     */
    public InConversationCondition(@Nullable final Variable<ConversationID> conversationID) {
        this.conversationID = conversationID;
    }

    @Override
    public boolean check(final Profile profile) throws QuestException {
        final Conversation conversation = Conversation.getConversation(profile);
        return conversation != null && (conversationID == null || conversation.getID().equals(conversationID.getValue(profile)));
    }
}
