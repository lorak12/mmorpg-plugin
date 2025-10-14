package org.nakii.mmorpg.quest.conversation;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.SectionIdentifier;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a conversation ID.
 */
public class ConversationID extends SectionIdentifier {

    /**
     * Creates new ConversationID instance.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package where the identifier was used in
     * @param identifier  the identifier of the conversation
     * @throws QuestException when the conversation could not be resolved with the given identifier
     */
    public ConversationID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "conversations", "Conversation");
    }
}
