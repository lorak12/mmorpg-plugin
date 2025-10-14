package org.nakii.mmorpg.quest.api.quest.event;

import org.nakii.mmorpg.quest.api.kernel.CoreQuestRegistry;

/**
 * Stores the event factories.
 */
public interface EventRegistry extends CoreQuestRegistry<PlayerEvent, PlayerlessEvent> {
}
