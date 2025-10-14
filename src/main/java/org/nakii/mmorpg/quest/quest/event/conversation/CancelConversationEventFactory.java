package org.nakii.mmorpg.quest.quest.event.conversation;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEventAdapter;

/**
 * Factory to create conversation cancel events from {@link Instruction}s.
 */
public class CancelConversationEventFactory implements PlayerEventFactory {
    /**
     * Logger factory to create a logger for the events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Create the conversation cancel event factory.
     *
     * @param loggerFactory the logger factory to create a logger for the events
     */
    public CancelConversationEventFactory(final BetonQuestLoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) {
        return new OnlineEventAdapter(
                new CancelConversationEvent(),
                loggerFactory.create(CancelConversationEvent.class),
                instruction.getPackage()
        );
    }
}
