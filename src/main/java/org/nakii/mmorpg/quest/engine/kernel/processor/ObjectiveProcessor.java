package org.nakii.mmorpg.quest.engine.kernel.processor;

import org.nakii.mmorpg.quest.engine.api.Objective;
import org.nakii.mmorpg.quest.engine.identifier.ObjectiveID;
import java.util.HashMap;
import java.util.Map;

// Simplified for now. Will be expanded later.
public class ObjectiveProcessor {
    private final Map<String, Objective> objectives = new HashMap<>();

    public void addObjective(String id, Objective objective) {
        objectives.put(id.toLowerCase(), objective);
    }

    public void clear() {
        objectives.clear();
    }
}