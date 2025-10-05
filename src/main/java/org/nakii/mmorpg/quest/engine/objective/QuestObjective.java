package org.nakii.mmorpg.quest.engine.objective;

import java.util.List;

/**
 * Base interface for all objective templates. An objective is a trackable goal for a player.
 */
public interface QuestObjective {
    String getObjectiveId();
    String getObjectiveType();
    String getDescription(); // <-- NEW
    List<String> getCompletionEvents();
}