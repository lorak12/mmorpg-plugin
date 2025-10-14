package org.nakii.mmorpg.quest.kernel.processor.adapter;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PlayerQuestFactory;
import org.nakii.mmorpg.quest.api.quest.PlayerlessQuestFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper for factories creating events.
 */
public class EventAdapterFactory extends QuestAdapterFactory<PlayerEvent, PlayerlessEvent, EventAdapter> {

    /**
     * Logger factory to create class specific logger for quest type factories.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Create a new adapter factory from {@link org.nakii.mmorpg.quest.api.quest QuestFactories} for
     * {@link org.nakii.mmorpg.quest.api.quest.event Events}.
     *
     * @param loggerFactory     the logger factory to create a new custom logger
     * @param questTypeApi      the QuestTypeAPi
     * @param playerFactory     the player factory to use
     * @param playerlessFactory the playerless factory to use
     * @throws IllegalArgumentException if no factory is given
     */
    public EventAdapterFactory(final BetonQuestLoggerFactory loggerFactory,
                               final QuestTypeApi questTypeApi, @Nullable final PlayerQuestFactory<PlayerEvent> playerFactory,
                               @Nullable final PlayerlessQuestFactory<PlayerlessEvent> playerlessFactory) {
        super(playerFactory, playerlessFactory);
        this.loggerFactory = loggerFactory;
        this.questTypeApi = questTypeApi;
    }

    @Override
    protected EventAdapter getAdapter(final Instruction instruction,
                                      @Nullable final PlayerEvent playerType,
                                      @Nullable final PlayerlessEvent playerlessType) throws QuestException {
        return new EventAdapter(loggerFactory.create(EventAdapter.class), questTypeApi, instruction, playerType, playerlessType);
    }
}
