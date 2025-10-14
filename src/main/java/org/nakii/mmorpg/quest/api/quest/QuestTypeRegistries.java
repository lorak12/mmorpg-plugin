package org.nakii.mmorpg.quest.api.quest;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.kernel.FeatureTypeRegistry;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionRegistry;
import org.nakii.mmorpg.quest.api.quest.event.EventRegistry;
import org.nakii.mmorpg.quest.api.quest.variable.VariableRegistry;

/**
 * Provides the BetonQuest Quest Type Registries.
 * <p>
 * They are used to add new implementations and access them.
 */
public interface QuestTypeRegistries {
    /**
     * Gets the registry for conditions.
     *
     * @return the condition registry
     */
    ConditionRegistry condition();

    /**
     * Gets the registry for events.
     *
     * @return the event registry
     */
    EventRegistry event();

    /**
     * Gets the registry for objectives.
     *
     * @return the objective registry
     */
    FeatureTypeRegistry<Objective> objective();

    /**
     * Gets the registry for variables.
     *
     * @return the variable registry
     */
    VariableRegistry variable();
}
