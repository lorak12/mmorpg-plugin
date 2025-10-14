package org.nakii.mmorpg.quest.kernel.registry.quest;

import org.nakii.mmorpg.quest.api.kernel.TypeFactory;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.PlayerQuestFactory;
import org.nakii.mmorpg.quest.api.quest.PlayerlessQuestFactory;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionRegistry;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.kernel.processor.adapter.ConditionAdapter;
import org.nakii.mmorpg.quest.kernel.processor.adapter.ConditionAdapterFactory;
import org.nakii.mmorpg.quest.kernel.registry.QuestTypeRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * Stores the condition types that can be used in BetonQuest.
 */
public class ConditionTypeRegistry extends QuestTypeRegistry<PlayerCondition, PlayerlessCondition, ConditionAdapter>
        implements ConditionRegistry {
    /**
     * Create a new condition type registry.
     *
     * @param log the logger that will be used for logging
     */
    public ConditionTypeRegistry(final BetonQuestLogger log) {
        super(log, "condition");
    }

    @Override
    protected TypeFactory<ConditionAdapter> getFactoryAdapter(
            @Nullable final PlayerQuestFactory<PlayerCondition> playerFactory,
            @Nullable final PlayerlessQuestFactory<PlayerlessCondition> playerlessFactory) {
        return new ConditionAdapterFactory(playerFactory, playerlessFactory);
    }
}
