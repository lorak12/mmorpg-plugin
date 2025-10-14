package org.nakii.mmorpg.quest.quest.event.scoreboard;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEventAdapter;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadEvent;

/**
 * Factory to create scoreboard tag events from {@link Instruction}s.
 */
public class ScoreboardTagEventFactory implements PlayerEventFactory {

    /**
     * Logger factory to create a logger for the events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Data used for condition check on the primary server thread.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create the scoreboard tag event factory.
     *
     * @param loggerFactory the logger factory to create a logger for the events
     * @param data          the data used for checking the condition on the main thread
     */
    public ScoreboardTagEventFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data) {
        this.loggerFactory = loggerFactory;
        this.data = data;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<ScoreboardTagAction> action = instruction.get(Argument.ENUM(ScoreboardTagAction.class));
        final String tag = instruction.next();
        final BetonQuestLogger logger = loggerFactory.create(ScoreboardTagEvent.class);
        return new PrimaryServerThreadEvent(new OnlineEventAdapter(
                new ScoreboardTagEvent(tag, action), logger, instruction.getPackage()
        ), data);
    }
}
