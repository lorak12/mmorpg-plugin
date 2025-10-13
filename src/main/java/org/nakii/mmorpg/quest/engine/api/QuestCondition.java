package org.nakii.mmorpg.quest.engine.api;

import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.profile.Profile;

@FunctionalInterface
public interface QuestCondition {
    boolean check(Profile profile) throws QuestException;
}