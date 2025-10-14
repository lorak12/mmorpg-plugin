package org.nakii.mmorpg.quest.compatibility.worldedit;

import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link WorldEditIntegrator} instances.
 */
public class WorldEditIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public WorldEditIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new WorldEditIntegrator();
    }
}
