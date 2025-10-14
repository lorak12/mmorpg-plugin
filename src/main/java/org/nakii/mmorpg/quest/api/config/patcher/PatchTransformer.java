package org.nakii.mmorpg.quest.api.config.patcher;

import org.nakii.mmorpg.quest.config.patcher.PatcherOptions;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Interface for transformers that transform a configuration.
 */
@FunctionalInterface
public interface PatchTransformer {

    /**
     * Applies a transformer to the given config.
     *
     * @param options options for the transformer
     * @param config  to transform
     * @throws PatchException if the transformation failed
     */
    void transform(PatcherOptions options, ConfigurationSection config) throws PatchException;
}
