package org.nakii.mmorpg.quest.compatibility.npc.citizens.event.move;

import org.nakii.mmorpg.quest.api.feature.FeatureApi;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.PrimaryServerThreadData;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEventAdapter;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadEvent;
import org.nakii.mmorpg.quest.api.quest.event.thread.PrimaryServerThreadPlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.npc.NpcID;
import org.nakii.mmorpg.quest.compatibility.npc.citizens.CitizensArgument;

/**
 * Factory for {@link CitizensStopEvent} from the {@link Instruction}.
 */
public class CitizensStopEventFactory implements PlayerlessEventFactory, PlayerEventFactory {

    /**
     * Feature API.
     */
    private final FeatureApi featureApi;

    /**
     * Required data for executing on the main thread.
     */
    private final PrimaryServerThreadData primaryServerThreadData;

    /**
     * Move Controller where to stop the NPC movement.
     */
    private final CitizensMoveController citizensMoveController;

    /**
     * Create a new NPCTeleportEventFactory.
     *
     * @param featureApi              the Feature API
     * @param primaryServerThreadData the data to use for syncing to the primary server thread
     * @param citizensMoveController  the move controller where to stop the NPC movement
     */
    public CitizensStopEventFactory(final FeatureApi featureApi, final PrimaryServerThreadData primaryServerThreadData, final CitizensMoveController citizensMoveController) {
        this.featureApi = featureApi;
        this.primaryServerThreadData = primaryServerThreadData;
        this.citizensMoveController = citizensMoveController;
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadPlayerlessEvent(createCitizensStopEvent(instruction), primaryServerThreadData);
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return new PrimaryServerThreadEvent(createCitizensStopEvent(instruction), primaryServerThreadData);
    }

    private NullableEventAdapter createCitizensStopEvent(final Instruction instruction) throws QuestException {
        final Variable<NpcID> npcId = instruction.get(CitizensArgument.CITIZENS_ID);
        return new NullableEventAdapter(new CitizensStopEvent(featureApi, npcId, citizensMoveController));
    }
}
