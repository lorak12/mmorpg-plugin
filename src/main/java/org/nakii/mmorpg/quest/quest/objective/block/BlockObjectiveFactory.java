package org.nakii.mmorpg.quest.quest.objective.block;

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
import org.nakii.mmorpg.quest.util.BlockSelector;
import org.bukkit.Location;

/**
 * Factory for creating {@link BlockObjective} instances from {@link Instruction}s.
 */
public class BlockObjectiveFactory implements ObjectiveFactory {

    /**
     * Logger factory to create a logger for the objectives.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * Creates a new instance of the BlockObjectiveFactory.
     *
     * @param loggerFactory the logger factory to create a logger for the objectives
     * @param pluginMessage the {@link PluginMessage} instance
     */
    public BlockObjectiveFactory(final BetonQuestLoggerFactory loggerFactory, final PluginMessage pluginMessage) {
        this.loggerFactory = loggerFactory;
        this.pluginMessage = pluginMessage;
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<BlockSelector> selector = instruction.get(Argument.BLOCK_SELECTOR);
        final boolean exactMatch = instruction.hasArgument("exactMatch");
        final Variable<Number> targetAmount = instruction.get(Argument.NUMBER);
        final boolean noSafety = instruction.hasArgument("noSafety");
        final Variable<Location> location = instruction.getValue("loc", Argument.LOCATION);
        final Variable<Location> region = instruction.getValue("region", Argument.LOCATION);
        final boolean ignoreCancel = instruction.hasArgument("ignorecancel");
        final BetonQuestLogger log = loggerFactory.create(BlockObjective.class);
        final IngameNotificationSender blockBreakSender = new IngameNotificationSender(log, pluginMessage, instruction.getPackage(),
                instruction.getID().getFull(), NotificationLevel.INFO, "blocks_to_break");
        final IngameNotificationSender blockPlaceSender = new IngameNotificationSender(log, pluginMessage, instruction.getPackage(),
                instruction.getID().getFull(), NotificationLevel.INFO, "blocks_to_place");
        return new BlockObjective(instruction, targetAmount, selector, exactMatch, noSafety, location, region, ignoreCancel,
                blockBreakSender, blockPlaceSender);
    }
}
