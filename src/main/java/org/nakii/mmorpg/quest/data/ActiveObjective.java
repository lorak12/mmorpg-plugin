package org.nakii.mmorpg.quest.data;

import org.nakii.mmorpg.quest.engine.objective.BlockObjective;
import org.nakii.mmorpg.quest.engine.objective.KillObjective;
import org.nakii.mmorpg.quest.engine.objective.QuestObjective;

/**
 * A wrapper around a QuestObjective template that holds a player's current progress.
 */
public class ActiveObjective {

    private final QuestObjective template;
    private int progress;

    public ActiveObjective(QuestObjective template) {
        this.template = template;
        this.progress = 0;
    }

    public String getObjectiveId() {
        return template.getObjectiveId();
    }

    public QuestObjective getTemplate() {
        return template;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void incrementProgress(int amount) {
        this.progress += amount;
    }

    public boolean isComplete() {
        if (template instanceof KillObjective killObjective) {
            return progress >= killObjective.getAmount();
        }
        if (template instanceof BlockObjective blockObjective) {
            return progress >= blockObjective.getAmount();
        }
        // For objectives without an "amount" (location, interact, die),
        // any progress (>= 1) means they are complete.
        return progress >= 1;
    }
}