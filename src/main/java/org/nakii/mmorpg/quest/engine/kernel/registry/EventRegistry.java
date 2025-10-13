package org.nakii.mmorpg.quest.engine.kernel.registry;

import org.nakii.mmorpg.quest.engine.api.EventFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EventRegistry {
    private final Map<String, EventFactory> factories = new HashMap<>();

    public void register(String name, EventFactory factory) {
        factories.put(name.toLowerCase(), factory);
    }

    public Optional<EventFactory> getFactory(String name) {
        return Optional.ofNullable(factories.get(name.toLowerCase()));
    }
}