package org.nakii.mmorpg.quest.kernel.registry.feature;

import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.conversation.interceptor.InterceptorFactory;
import org.nakii.mmorpg.quest.kernel.registry.FactoryRegistry;

/**
 * Stores the Interceptors that can be used in BetonQuest.
 */
public class InterceptorRegistry extends FactoryRegistry<InterceptorFactory> {

    /**
     * Create a new Interceptor registry.
     *
     * @param log the logger that will be used for logging
     */
    public InterceptorRegistry(final BetonQuestLogger log) {
        super(log, "Interceptor");
    }
}
