package org.nakii.mmorpg.quest.compatibility.npc.znpcsplus;

import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link ZNPCsPlusIntegrator} instances.
 */
public class ZNPCsPlusIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public ZNPCsPlusIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new ZNPCsPlusIntegrator();
    }
}
