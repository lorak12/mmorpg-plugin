package org.nakii.mmorpg.quest.quest.event.random;

import org.nakii.mmorpg.quest.api.quest.event.EventID;

/**
 * Represents an event with its weight.
 *
 * @param eventID the event to be executed
 * @param weight  the weight of the event
 */
public record RandomEvent(EventID eventID, double weight) {
}
