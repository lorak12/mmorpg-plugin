package org.nakii.mmorpg.quest.id;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.InstructionIdentifier;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a journal entry ID.
 */
public class JournalEntryID extends InstructionIdentifier {

    /**
     * Creates new JournalEntryID instance.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package where the identifier was used in
     * @param identifier  the identifier of the quest compass
     * @throws QuestException if the instruction could not be created or
     *                        when the journal entry could not be resolved with the given identifier
     */
    public JournalEntryID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "journal", "Journal Entry");
    }
}
