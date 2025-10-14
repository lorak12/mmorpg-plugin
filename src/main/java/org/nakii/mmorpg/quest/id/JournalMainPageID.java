package org.nakii.mmorpg.quest.id;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.InstructionIdentifier;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Journal Main Page ID.
 */
public class JournalMainPageID extends InstructionIdentifier {

    /**
     * Creates new JournalMainPageID instance.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package where the identifier was used in
     * @param identifier  the identifier of the quest compass
     * @throws QuestException if the instruction could not be created or
     *                        when the main page could not be resolved with the given identifier
     */
    public JournalMainPageID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "journal_main_page", "Journal Main Page");
    }
}
