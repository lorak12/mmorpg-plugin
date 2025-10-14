package org.nakii.mmorpg.quest.api.identifier;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.quest.QuestException;

/**
 * An ID that does not have an actual ID.
 * This is used for runtime-only IDs that are not stored anywhere.
 */
@SuppressWarnings("PMD.ShortClassName")
public class NoID extends Identifier {

    /**
     * Constructs a new NoID.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the quest package to use
     * @throws QuestException if the ID cannot be created
     */
    public NoID(final QuestPackageManager packManager, final QuestPackage pack) throws QuestException {
        super(packManager, pack, "NoID");
    }
}
