package org.nakii.mmorpg.quest.quest.condition.ride;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.condition.online.OnlineConditionAdapter;
import org.nakii.mmorpg.quest.api.quest.condition.thread.PrimaryServerThreadPlayerCondition;
import org.bukkit.entity.EntityType;

/**
 * Factory to create ride conditions from {@link Instruction}s.
 */
public class RideConditionFactory implements PlayerConditionFactory {

    /**
     * The string to match any entity.
     */
    private static final String ANY_ENTITY = "any";

    /**
     * Logger factory to create a logger for the conditions.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Data used for condition check on the primary server thread.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create the ride condition factory.
     *
     * @param loggerFactory the logger factory to create a logger for the conditions
     * @param data          the data used for checking the condition on the main thread
     */
    public RideConditionFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data) {
        this.loggerFactory = loggerFactory;
        this.data = data;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final String name = instruction.next();
        final Variable<EntityType> vehicle;
        if (ANY_ENTITY.equalsIgnoreCase(name)) {
            vehicle = null;
        } else {
            vehicle = instruction.get(name, Argument.ENUM(EntityType.class));
        }
        final BetonQuestLogger logger = loggerFactory.create(RideCondition.class);
        return new PrimaryServerThreadPlayerCondition(
                new OnlineConditionAdapter(new RideCondition(vehicle), logger, instruction.getPackage()), data);
    }
}
