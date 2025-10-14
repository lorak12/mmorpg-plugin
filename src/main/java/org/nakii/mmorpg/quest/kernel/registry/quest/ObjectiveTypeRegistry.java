package org.nakii.mmorpg.quest.kernel.registry.quest;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.kernel.registry.FactoryTypeRegistry;

/**
 * Stores the Objectives that can be used in BetonQuest.
 */
public class ObjectiveTypeRegistry extends FactoryTypeRegistry<Objective> {

    /**
     * Create a new Objective registry.
     *
     * @param log the logger that will be used for logging
     */
    public ObjectiveTypeRegistry(final BetonQuestLogger log) {
        super(log, "objective");
    }
}
