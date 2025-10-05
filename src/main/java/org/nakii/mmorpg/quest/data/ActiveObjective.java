package org.nakii.mmorpg.quest.data;


import org.nakii.mmorpg.quest.engine.objective.KillObjective;
import org.nakii.mmorpg.quest.engine.objective.QuestObjective;

/**
 * Represents a player's current progress on a specific objective.
 * This object is part of PlayerQuestData and is serialized to the database.
 */
public class ActiveObjective {

    private final String objectiveId;
    private final QuestObjective template;
    private int progress;

    public ActiveObjective(QuestObjective template) {
        this.objectiveId = template.getObjectiveId();
        this.template = template;
        this.progress = 0;
    }

    public String getObjectiveId() {
        return objectiveId;
    }

    public QuestObjective getTemplate() {
        return template;
    }

    public int getProgress() {
        return progress;
    }

    public void incrementProgress(int amount) {
        this.progress += amount;
    }

    public boolean isComplete() {
        if (template instanceof KillObjective killObjective) {
            return progress >= killObjective.getAmount();
        }
        // Add other objective type checks here
        return false;
    }
}