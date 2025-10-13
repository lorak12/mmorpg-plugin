package org.nakii.mmorpg.quest.engine.kernel;

import org.nakii.mmorpg.quest.engine.api.QuestApi;
import org.nakii.mmorpg.quest.engine.identifier.ConditionID;
import org.nakii.mmorpg.quest.engine.identifier.EventID;
import org.nakii.mmorpg.quest.engine.kernel.processor.ConditionProcessor;
import org.nakii.mmorpg.quest.engine.kernel.processor.EventProcessor;
import org.nakii.mmorpg.quest.engine.kernel.processor.ObjectiveProcessor;
import org.nakii.mmorpg.quest.engine.kernel.registry.ConditionRegistry;
import org.nakii.mmorpg.quest.engine.kernel.registry.EventRegistry;
import org.nakii.mmorpg.quest.engine.kernel.registry.ObjectiveRegistry;
import org.nakii.mmorpg.quest.engine.profile.Profile;

// This class holds all the managers and implements the main API
public class CoreRegistry implements QuestApi {

    private final ConditionRegistry conditionRegistry = new ConditionRegistry();
    private final EventRegistry eventRegistry = new EventRegistry();
    private final ObjectiveRegistry objectiveRegistry = new ObjectiveRegistry();

    private final ConditionProcessor conditionProcessor = new ConditionProcessor();
    private final EventProcessor eventProcessor = new EventProcessor();
    private final ObjectiveProcessor objectiveProcessor = new ObjectiveProcessor();

    // Public getters for the registries so we can add factories to them
    public ConditionRegistry getConditionRegistry() { return conditionRegistry; }
    public EventRegistry getEventRegistry() { return eventRegistry; }
    public ObjectiveRegistry getObjectiveRegistry() { return objectiveRegistry; }

    // Public getters for the processors so the QuestManager can load data into them
    public ConditionProcessor getConditionProcessor() { return conditionProcessor; }
    public EventProcessor getEventProcessor() { return eventProcessor; }
    public ObjectiveProcessor getObjectiveProcessor() { return objectiveProcessor; }

    @Override
    public boolean checkCondition(Profile profile, ConditionID conditionID) {
        return conditionProcessor.check(profile, conditionID);
    }

    @Override
    public void fireEvent(Profile profile, EventID eventID) {
        eventProcessor.fire(profile, eventID);
    }
}