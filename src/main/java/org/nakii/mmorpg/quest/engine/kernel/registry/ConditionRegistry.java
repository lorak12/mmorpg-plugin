package org.nakii.mmorpg.quest.engine.kernel.registry;

import org.nakii.mmorpg.quest.engine.api.ConditionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConditionRegistry {
    private final Map<String, ConditionFactory> factories = new HashMap<>();

    public void register(String name, ConditionFactory factory) {
        factories.put(name.toLowerCase(), factory);
    }

    public Optional<ConditionFactory> getFactory(String name) {
        return Optional.ofNullable(factories.get(name.toLowerCase()));
    }
}