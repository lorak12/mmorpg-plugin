package org.nakii.mmorpg.quest.quest.condition.objective;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveID;

/**
 * A factory for creating ObjectiveConditions.
 */
public class ObjectiveConditionFactory implements PlayerConditionFactory {

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Creates a new ObjectiveConditionFactory.
     *
     * @param questTypeApi the Quest Type API
     */
    public ObjectiveConditionFactory(final QuestTypeApi questTypeApi) {
        this.questTypeApi = questTypeApi;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        return new ObjectiveCondition(questTypeApi, instruction.get(ObjectiveID::new));
    }
}
