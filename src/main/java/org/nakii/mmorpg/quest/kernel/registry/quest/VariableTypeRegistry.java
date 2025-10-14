package org.nakii.mmorpg.quest.kernel.registry.quest;

import org.nakii.mmorpg.quest.api.kernel.TypeFactory;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.PlayerQuestFactory;
import org.nakii.mmorpg.quest.api.quest.PlayerlessQuestFactory;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariable;
import org.nakii.mmorpg.quest.api.quest.variable.VariableRegistry;
import org.nakii.mmorpg.quest.kernel.processor.adapter.VariableAdapter;
import org.nakii.mmorpg.quest.kernel.processor.adapter.VariableAdapterFactory;
import org.nakii.mmorpg.quest.kernel.registry.QuestTypeRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the variable types that can be used in BetonQuest.
 */
public class VariableTypeRegistry extends QuestTypeRegistry<PlayerVariable, PlayerlessVariable, VariableAdapter>
        implements VariableRegistry {

    /**
     * Create a new variable type registry.
     *
     * @param log the logger that will be used for logging
     */
    public VariableTypeRegistry(final BetonQuestLogger log) {
        super(log, "variable");
    }

    @Override
    protected TypeFactory<VariableAdapter> getFactoryAdapter(
            @Nullable final PlayerQuestFactory<PlayerVariable> playerFactory,
            @Nullable final PlayerlessQuestFactory<PlayerlessVariable> playerlessFactory) {
        return new VariableAdapterFactory(playerFactory, playerlessFactory);
    }
}
