package org.nakii.mmorpg.quest.api.quest.event;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.identifier.InstructionIdentifier;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.jetbrains.annotations.Nullable;

/**
 * ID of an Event.
 */
public class EventID extends InstructionIdentifier {

    /**
     * Create a new Event ID.
     *
     * @param packManager the quest package manager to get quest packages from
     * @param pack        the package of the event
     * @param identifier  the complete identifier of the event
     * @throws QuestException if there is no such event
     */
    public EventID(final QuestPackageManager packManager, @Nullable final QuestPackage pack, final String identifier) throws QuestException {
        super(packManager, pack, identifier, "events", "Event");
    }
}
