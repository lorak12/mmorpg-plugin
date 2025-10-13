package org.nakii.mmorpg.quest.engine.kernel.processor;

import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.api.QuestEvent;
import org.nakii.mmorpg.quest.engine.identifier.EventID;
import org.nakii.mmorpg.quest.engine.profile.Profile;

import java.util.HashMap;
import java.util.Map;

public class EventProcessor {
    private final Map<String, QuestEvent> events = new HashMap<>();

    public void addEvent(String id, QuestEvent event) {
        events.put(id.toLowerCase(), event);
    }

    public void clear() {
        events.clear();
    }

    public void fire(Profile profile, EventID eventID) {
        QuestEvent event = events.get(eventID.getFull().toLowerCase());
        if (event == null) {
            System.err.println("Warning: Unknown event '" + eventID.getFull() + "'");
            return;
        }
        try {
            event.execute(profile);
        } catch (QuestException e) {
            System.err.println("Error firing event '" + eventID.getFull() + "': " + e.getMessage());
        }
    }
}