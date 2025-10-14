package org.nakii.mmorpg.quest.quest.event.compass;

import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadEvent;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.nakii.mmorpg.quest.id.CompassID;

/**
 * The compass event factory.
 */
public class CompassEventFactory implements PlayerEventFactory {
    /**
     * Feature API.
     */
    private final FeatureApi featureApi;

    /**
     * Storage to get the offline player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * Data for primary server thread access.
     */
    private final PrimaryServerThreadData data;

    /**
     * Create the compass event factory.
     *
     * @param featureApi  the Feature API
     * @param dataStorage the storage for used player data
     * @param data        the data for primary server thread access
     */
    public CompassEventFactory(final FeatureApi featureApi, final PlayerDataStorage dataStorage,
                               final PrimaryServerThreadData data) {
        this.featureApi = featureApi;
        this.dataStorage = dataStorage;
        this.data = data;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<CompassTargetAction> action = instruction.get(Argument.ENUM(CompassTargetAction.class));
        final Variable<CompassID> compassId = instruction.get(CompassID::new);
        return new PrimaryServerThreadEvent(
                new CompassEvent(featureApi, dataStorage, action, compassId),
                data);
    }
}
