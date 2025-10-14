package org.nakii.mmorpg.quest.quest.event.cancel;

import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLoggerFactory;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEventAdapter;
import org.nakii.mmorpg.quest.id.QuestCancelerID;

/**
 * Factory for the cancel event.
 */
public class CancelEventFactory implements PlayerEventFactory {
    /**
     * Logger factory to create a logger for the events.
     */
    private final BetonQuestLoggerFactory loggerFactory;

    /**
     * Feature API.
     */
    private final FeatureApi featureApi;

    /**
     * Creates a new cancel event factory.
     *
     * @param loggerFactory the logger factory to create a logger for the events
     * @param featureApi    the feature API
     */
    public CancelEventFactory(final BetonQuestLoggerFactory loggerFactory, final FeatureApi featureApi) {
        this.loggerFactory = loggerFactory;
        this.featureApi = featureApi;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<QuestCancelerID> cancelerID = instruction.get(QuestCancelerID::new);
        final boolean bypass = instruction.hasArgument("bypass");
        return new OnlineEventAdapter(new CancelEvent(featureApi, cancelerID, bypass),
                loggerFactory.create(CancelEvent.class), instruction.getPackage());
    }
}
