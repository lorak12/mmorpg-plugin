package org.nakii.mmorpg.quest.quest.event.objective;

import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEventAdapter;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveID;
import org.nakii.mmorpg.quest.database.PlayerDataFactory;

import java.util.List;
import java.util.Locale;

/**
 * Factory for {@link ObjectiveEvent}s.
 */
public class ObjectiveEventFactory implements PlayerEventFactory, PlayerlessEventFactory {

    /**
     * The BetonQuest instance.
     */
    private final QuestModule questModule;

    /**
     * Logger factory to create a logger for the events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Factory to create new Player Data.
     */
    private final PlayerDataFactory playerDataFactory;

    /**
     * Creates a new factory for {@link ObjectiveEvent}s.
     *
     * @param questModule        the BetonQuest instance
     * @param loggerFactory     the logger factory to create a logger for the events
     * @param questTypeApi      the Quest Type API
     * @param playerDataFactory the factory to create player data
     */
    public ObjectiveEventFactory(final QuestModule questModule, final BetonQuestLoggerFactory loggerFactory,
                                 final QuestTypeApi questTypeApi, final PlayerDataFactory playerDataFactory) {
        this.questModule = questModule;
        this.loggerFactory = loggerFactory;
        this.questTypeApi = questTypeApi;
        this.playerDataFactory = playerDataFactory;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return createObjectiveEvent(instruction);
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return createObjectiveEvent(instruction);
    }

    private NullableEventAdapter createObjectiveEvent(final Instruction instruction) throws QuestException {
        final String action = instruction.next().toLowerCase(Locale.ROOT);
        final Variable<List<ObjectiveID>> objectives = instruction.getList(ObjectiveID::new);
        return new NullableEventAdapter(new ObjectiveEvent(questModule, loggerFactory.create(ObjectiveEvent.class),
                questTypeApi, instruction.getPackage(), objectives, playerDataFactory, action));
    }
}
