package org.nakii.mmorpg.quest.kernel.registry.quest;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.api.BetonQuestApi;
import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestTypeRegistries;
import org.nakii.mmorpg.quest.kernel.registry.FactoryTypeRegistry;

/**
 * Registry for quest core elements.
 *
 * @param condition The Registry holding registered condition types.
 * @param event     The Registry holding registered event types.
 * @param objective The Registry holding registered objective types.
 * @param variable  The Registry holding registered variable types.
 */
public record BaseQuestTypeRegistries(
        ConditionTypeRegistry condition,
        EventTypeRegistry event,
        FactoryTypeRegistry<Objective> objective,
        VariableTypeRegistry variable
) implements QuestTypeRegistries {

    /**
     * Create a new quest registry for quest core elements.
     *
     * @param loggerFactory the logger factory to create individual class logger
     * @param betonQuest    the plugin instance to get QuestTypeApi from once initialized
     * @return the newly created registries
     */
    public static BaseQuestTypeRegistries create(final BetonQuestLoggerFactory loggerFactory, final BetonQuestApi betonQuest) {
        return new BaseQuestTypeRegistries(
                new ConditionTypeRegistry(loggerFactory.create(ConditionTypeRegistry.class)),
                new EventTypeRegistry(loggerFactory.create(EventTypeRegistry.class), loggerFactory, betonQuest),
                new ObjectiveTypeRegistry(loggerFactory.create(ObjectiveTypeRegistry.class)),
                new VariableTypeRegistry(loggerFactory.create(VariableTypeRegistry.class))
        );
    }
}
