package org.nakii.mmorpg.quest.engine.api;

import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.profile.Profile;

@FunctionalInterface
public interface QuestEvent {
    void execute(Profile profile) throws QuestException;
}