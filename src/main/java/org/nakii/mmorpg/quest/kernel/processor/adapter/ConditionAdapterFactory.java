package org.nakii.mmorpg.quest.kernel.processor.adapter;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.PlayerQuestFactory;
import org.nakii.mmorpg.quest.api.quest.PlayerlessQuestFactory;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.jetbrains.annotations.Nullable;

/**
 * Adapter to let {@link PlayerConditionFactory ConditionFactories} create
 * {@link PlayerCondition}s and {@link PlayerlessCondition}s they create.
 */
public class ConditionAdapterFactory extends QuestAdapterFactory<PlayerCondition, PlayerlessCondition, ConditionAdapter> {
    /**
     * Create a new adapter factory from {@link org.nakii.mmorpg.quest.api.quest QuestFactories} for
     * {@link org.nakii.mmorpg.quest.api.quest.condition Conditions}.
     *
     * @param playerFactory     the player factory to use
     * @param playerlessFactory the playerless factory to use
     * @throws IllegalArgumentException if no factory is given
     */
    public ConditionAdapterFactory(@Nullable final PlayerQuestFactory<PlayerCondition> playerFactory,
                                   @Nullable final PlayerlessQuestFactory<PlayerlessCondition> playerlessFactory) {
        super(playerFactory, playerlessFactory);
    }

    @Override
    protected ConditionAdapter getAdapter(final Instruction instruction,
                                          @Nullable final PlayerCondition playerType,
                                          @Nullable final PlayerlessCondition playerlessType) {
        return new ConditionAdapter(instruction.getPackage(), playerType, playerlessType);
    }
}
