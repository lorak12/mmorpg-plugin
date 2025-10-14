package org.nakii.mmorpg.quest.compatibility.vault;

import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link VaultIntegrator} instances.
 */
public class VaultIntegratorFactory implements IntegratorFactory {
    /**
     * Creates a new instance of the factory.
     */
    public VaultIntegratorFactory() {
    }

    @Override
    public Integrator getIntegrator() {
        return new VaultIntegrator();
    }
}
