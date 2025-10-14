package org.nakii.mmorpg.quest.api.quest.objective;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.InstructionIdentifier;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * ID of an Objective.
 */
public class ObjectiveID extends InstructionIdentifier {

    /**
     * Create a new Objective ID.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package of the objective
     * @param identifier  the complete identifier of the objective
     * @throws QuestException if there is no such objective
     */
    public ObjectiveID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "objectives", "Objective");
    }
}
