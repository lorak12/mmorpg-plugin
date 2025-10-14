package org.nakii.mmorpg.quest.compatibility.worldguard;

import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link WorldGuardIntegrator} instances.
 */
public class WorldGuardIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public WorldGuardIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new WorldGuardIntegrator();
    }
}
