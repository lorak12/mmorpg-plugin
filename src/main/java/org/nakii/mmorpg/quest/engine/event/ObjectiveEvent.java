package org.nakii.mmorpg.quest.engine.event;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.model.QuestPackage;

public class ObjectiveEvent implements QuestEvent {

    private final String objectiveId;
    private final Action action;

    public enum Action { START, DELETE }

    public ObjectiveEvent(String objectiveId, Action action) {
        this.objectiveId = objectiveId;
        this.action = action;
    }

    @Override
    public void execute(Player player, MMORPGCore plugin, QuestPackage context) {
        if (action == Action.START) {
            plugin.getQuestManager().startObjective(player, objectiveId, context);
        } else {
            plugin.getQuestManager().deleteObjective(player, objectiveId);
        }
    }
}