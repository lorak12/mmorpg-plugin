package org.nakii.mmorpg.quest.quest.objective.experience;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.quest.event.IngameNotificationSender;
import org.nakii.mmorpg.quest.quest.event.NotificationLevel;

/**
 * Factory for creating {@link ExperienceObjective} instances from {@link Instruction}s.
 */
public class ExperienceObjectiveFactory implements ObjectiveFactory {

    /**
     * Logger factory to create a logger for the objectives.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * Creates a new instance of the ExperienceObjectiveFactory.
     *
     * @param loggerFactory the logger factory to create a logger for the objectives
     * @param pluginMessage the {@link PluginMessage} instance
     */
    public ExperienceObjectiveFactory(final BetonQuestLoggerFactory loggerFactory, final PluginMessage pluginMessage) {
        this.loggerFactory = loggerFactory;
        this.pluginMessage = pluginMessage;
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Number> amount = instruction.get(Argument.NUMBER);
        final BetonQuestLogger log = loggerFactory.create(ExperienceObjective.class);
        final IngameNotificationSender levelSender = new IngameNotificationSender(log,
                pluginMessage, instruction.getPackage(), instruction.getID().getFull(),
                NotificationLevel.INFO, "level_to_gain");
        return new ExperienceObjective(instruction, amount, levelSender);
    }
}
