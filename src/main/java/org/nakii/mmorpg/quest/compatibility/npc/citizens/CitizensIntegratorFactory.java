package org.nakii.mmorpg.quest.compatibility.npc.citizens;

import org.nakii.mmorpg.quest.compatibility.Compatibility;
import org.nakii.mmorpg.quest.compatibility.Integrator;
import org.nakii.mmorpg.quest.compatibility.IntegratorFactory;

/**
 * Factory for creating {@link CitizensIntegrator} instances.
 */
public class CitizensIntegratorFactory implements IntegratorFactory {
    /**
     * The compatibility instance to use for checking other hooks.
     */
    private final Compatibility compatibility;

    /**
     * Creates a new Citizens integrator factory.
     *
     * @param compatibility the compatibility instance to use for checking other hooks
     */
    public CitizensIntegratorFactory(final Compatibility compatibility) {
        this.compatibility = compatibility;
    }

    @Override
    public Integrator getIntegrator() {
        return new CitizensIntegrator(compatibility);
    }
}
