package org.nakii.mmorpg.quest.kernel.registry.feature;

import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.kernel.registry.FactoryRegistry;
import org.nakii.mmorpg.quest.notify.NotifyIOFactory;

/**
 * Stores the Notify IOs that can be used in BetonQuest.
 */
public class NotifyIORegistry extends FactoryRegistry<NotifyIOFactory> {

    /**
     * Create a new NotifyIO registry.
     *
     * @param log the logger that will be used for logging
     */
    public NotifyIORegistry(final BetonQuestLogger log) {
        super(log, "Notify IO");
    }
}
