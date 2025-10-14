package org.nakii.mmorpg.quest.compatibility.effectlib;

import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link EffectLibIntegrator} instances.
 */
public class EffectLibIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public EffectLibIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new EffectLibIntegrator();
    }
}
