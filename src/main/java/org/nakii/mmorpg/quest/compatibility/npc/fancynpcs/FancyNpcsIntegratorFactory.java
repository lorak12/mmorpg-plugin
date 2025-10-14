package org.nakii.mmorpg.quest.compatibility.npc.fancynpcs;

import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link FancyNpcsIntegrator} instances.
 */
public class FancyNpcsIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public FancyNpcsIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new FancyNpcsIntegrator();
    }
}
