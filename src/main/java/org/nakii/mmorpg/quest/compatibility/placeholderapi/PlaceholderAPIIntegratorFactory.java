package org.nakii.mmorpg.quest.compatibility.placeholderapi;

import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link PlaceholderAPIIntegrator} instances.
 */
public class PlaceholderAPIIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public PlaceholderAPIIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new PlaceholderAPIIntegrator();
    }
}
