package org.nakii.mmorpg.quest.quest.condition.conversation;

import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.conversation.ConversationID;

/**
 * Checks if the conversation with player has at least one possible option.
 */
public class ConversationCondition implements PlayerCondition {

    /**
     * Feature API.
     */
    private final FeatureApi featureApi;

    /**
     * The conversation to check.
     */
    private final Variable<ConversationID> conversationID;

    /**
     * Creates a new ConversationCondition.
     *
     * @param featureApi     the feature API
     * @param conversationID the conversation to check
     */
    public ConversationCondition(final FeatureApi featureApi, final Variable<ConversationID> conversationID) {
        this.featureApi = featureApi;
        this.conversationID = conversationID;
    }

    @Override
    public boolean check(final Profile profile) throws QuestException {
        return featureApi.getConversation(conversationID.getValue(profile)).isReady(profile);
    }
}
