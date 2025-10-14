package org.nakii.mmorpg.quest.quest.condition.logik;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.quest.condition.nullable.NullableCondition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * All of specified conditions have to be true.
 */
public class ConjunctionCondition implements NullableCondition {

    /**
     * All of specified conditions have to be true.
     */
    private final Variable<List<ConditionID>> conditions;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Constructor for the {@link ConjunctionCondition} class.
     *
     * @param conditions   All of specified conditions have to be true.
     * @param questTypeApi the Quest Type API
     */
    public ConjunctionCondition(final Variable<List<ConditionID>> conditions, final QuestTypeApi questTypeApi) {
        this.conditions = conditions;
        this.questTypeApi = questTypeApi;
    }

    @Override
    public boolean check(@Nullable final Profile profile) throws QuestException {
        return questTypeApi.conditions(profile, conditions.getValue(profile));
    }
}
