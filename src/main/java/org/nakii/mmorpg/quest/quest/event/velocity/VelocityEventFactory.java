package org.nakii.mmorpg.quest.quest.event.velocity;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEventAdapter;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadEvent;
import org.bukkit.util.Vector;

/**
 * Factory to create velocity events from {@link Instruction}s.
 */
public class VelocityEventFactory implements PlayerEventFactory {
    /**
     * Logger factory to create a logger for the events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Data for primary server thread access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create the velocity event factory.
     *
     * @param loggerFactory the logger factory to create a logger for the events
     * @param data          the data for primary server thread access
     */
    public VelocityEventFactory(final BetonQuestLoggerFactory loggerFactory, final PrimaryServerThreadData data) {
        this.loggerFactory = loggerFactory;
        this.data = data;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<Vector> vector = instruction.getValue("vector", Argument.VECTOR);
        if (vector == null) {
            throw new QuestException("A 'vector' is required");
        }
        final Variable<VectorDirection> direction = instruction.getValue("direction", Argument.ENUM(VectorDirection.class), VectorDirection.ABSOLUTE);
        final Variable<VectorModification> modification = instruction.getValue("modification", Argument.ENUM(VectorModification.class), VectorModification.SET);
        return new PrimaryServerThreadEvent(new OnlineEventAdapter(
                new VelocityEvent(vector, direction, modification),
                loggerFactory.create(VelocityEvent.class),
                instruction.getPackage()
        ), data);
    }
}
