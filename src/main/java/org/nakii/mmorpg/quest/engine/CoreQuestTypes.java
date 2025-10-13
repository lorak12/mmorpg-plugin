package org.nakii.mmorpg.quest.engine;

import org.nakii.mmorpg.quest.engine.api.ConditionFactory;
import org.nakii.mmorpg.quest.engine.api.EventFactory;
import org.nakii.mmorpg.quest.engine.conditions.TagCondition;
import org.nakii.mmorpg.quest.engine.events.TagEvent;
import org.nakii.mmorpg.quest.engine.kernel.CoreRegistry;

import java.util.Locale;

public class CoreQuestTypes {

    public void register(CoreRegistry registry) {
        // Register Conditions
        registry.getConditionRegistry().register("tag", TagCondition::new);

        // Register Events
        registry.getEventRegistry().register("tag", instruction -> {
            String action = instruction.getPart(1).toLowerCase(Locale.ROOT);
            switch (action) {
                case "add":
                    return new TagEvent(instruction, true);
                case "remove":
                case "delete":
                    return new TagEvent(instruction, false);
                default:
                    throw new QuestException("Unknown tag event action: " + action + ". Use 'add' or 'remove'.");
            }
        });
    }
}