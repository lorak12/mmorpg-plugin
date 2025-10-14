package org.nakii.mmorpg.quest.quest.event.folder;

import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.quest.event.*;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEventAdapter;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Factory to create {@link FolderEvent} instances.
 */
public class FolderEventFactory implements PlayerEventFactory, PlayerlessEventFactory {

    /**
     * The BetonQuest instance.
     */
    private final QuestModule questModule;

    /**
     * Logger factory to create a logger for the events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * The plugin manager to register the quit listener.
     */
    private final PluginManager pluginManager;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Create a new folder event factory.
     *
     * @param questModule    the BetonQuest instance
     * @param loggerFactory the logger factory to create a logger for the events
     * @param pluginManager the plugin manager to register the quit listener
     * @param questTypeApi  the Quest Type API
     */
    public FolderEventFactory(final QuestModule questModule, final BetonQuestLoggerFactory loggerFactory,
                              final PluginManager pluginManager, final QuestTypeApi questTypeApi) {
        this.questModule = questModule;
        this.loggerFactory = loggerFactory;
        this.pluginManager = pluginManager;
        this.questTypeApi = questTypeApi;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return createFolderEvent(instruction);
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return createFolderEvent(instruction);
    }

    private NullableEventAdapter createFolderEvent(final Instruction instruction) throws QuestException {
        final Variable<List<EventID>> events = instruction.getList(EventID::new);
        final Variable<Number> delay = instruction.getValue("delay", Argument.NUMBER);
        final Variable<Number> period = instruction.getValue("period", Argument.NUMBER);
        final Variable<Number> random = instruction.getValue("random", Argument.NUMBER);
        final Variable<TimeUnit> timeUnit = instruction.getValue("unit", this::getTimeUnit, TimeUnit.SECONDS);
        final boolean cancelOnLogout = instruction.hasArgument("cancelOnLogout");
        final Variable<List<ConditionID>> cancelConditions = instruction.getValueList("cancelConditions", ConditionID::new);
        return new NullableEventAdapter(new FolderEvent(questModule, loggerFactory.create(FolderEvent.class), pluginManager,
                events,
                questTypeApi, new Random(), delay, period, random, timeUnit, cancelOnLogout, cancelConditions));
    }

    private TimeUnit getTimeUnit(final String input) throws QuestException {
        return switch (input.toLowerCase(Locale.ROOT)) {
            case "ticks" -> TimeUnit.TICKS;
            case "seconds" -> TimeUnit.SECONDS;
            case "minutes" -> TimeUnit.MINUTES;
            default ->
                    throw new QuestException("Invalid time unit: " + input + ". Valid units are: ticks, seconds, minutes.");
        };
    }
}
