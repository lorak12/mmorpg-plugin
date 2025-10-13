package org.nakii.mmorpg.quest.engine.kernel.registry;

import org.nakii.mmorpg.quest.engine.api.ObjectiveFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ObjectiveRegistry {
    private final Map<String, ObjectiveFactory> factories = new HashMap<>();

    public void register(String name, ObjectiveFactory factory) {
        factories.put(name.toLowerCase(), factory);
    }

    public Optional<ObjectiveFactory> getFactory(String name) {
        return Optional.ofNullable(factories.get(name.toLowerCase()));
    }
}