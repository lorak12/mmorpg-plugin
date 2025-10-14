package org.nakii.mmorpg.quest.kernel.processor.adapter;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.PlayerQuestFactory;
import org.nakii.mmorpg.quest.api.quest.PlayerlessQuestFactory;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariable;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for factories creating variables.
 */
public class VariableAdapterFactory extends QuestAdapterFactory<PlayerVariable, PlayerlessVariable, VariableAdapter> {

    /**
     * Create a new adapter factory from {@link org.nakii.mmorpg.quest.api.quest QuestFactories} for
     * {@link org.nakii.mmorpg.quest.api.quest.variable Variables}.
     *
     * @param playerFactory     the player factory to use
     * @param playerlessFactory the playerless factory to use
     * @throws IllegalArgumentException if no factory is given
     */
    public VariableAdapterFactory(@Nullable final PlayerQuestFactory<PlayerVariable> playerFactory,
                                  @Nullable final PlayerlessQuestFactory<PlayerlessVariable> playerlessFactory) {
        super(playerFactory, playerlessFactory);
    }

    @Override
    protected VariableAdapter getAdapter(final Instruction instruction,
                                         @Nullable final PlayerVariable playerType,
                                         @Nullable final PlayerlessVariable playerlessType) {
        return new VariableAdapter(instruction, playerType, playerlessType);
    }
}
