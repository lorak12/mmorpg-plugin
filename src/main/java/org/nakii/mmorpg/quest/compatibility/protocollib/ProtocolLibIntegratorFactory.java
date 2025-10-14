package org.nakii.mmorpg.quest.compatibility.protocollib;

import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link ProtocolLibIntegrator} instances.
 */
public class ProtocolLibIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public ProtocolLibIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new ProtocolLibIntegrator();
    }
}
