package org.nakii.mmorpg.quest.kernel.registry.feature;

import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.conversation.ConversationIOFactory;
import org.nakii.mmorpg.quest.kernel.registry.FactoryRegistry;

/**
 * Stores the Conversation IOs that can be used in BetonQuest.
 */
public class ConversationIORegistry extends FactoryRegistry<ConversationIOFactory> {

    /**
     * Create a new ConversationIO registry.
     *
     * @param log the logger that will be used
     */
    public ConversationIORegistry(final BetonQuestLogger log) {
        super(log, "Conversation IO");
    }
}
