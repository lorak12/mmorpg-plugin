package org.nakii.mmorpg.quest.engine.api;

import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.identifier.ConditionID;
import org.nakii.mmorpg.quest.engine.identifier.EventID;
import org.nakii.mmorpg.quest.engine.profile.Profile;

public interface QuestApi {
    boolean checkCondition(Profile profile, ConditionID conditionID);
    void fireEvent(Profile profile, EventID eventID);
}