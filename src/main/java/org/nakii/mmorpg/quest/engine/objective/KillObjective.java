package org.nakii.mmorpg.quest.engine.objective;
import org.nakii.mmorpg.quest.engine.objective.QuestObjective;

import java.util.Collections;
import java.util.List;
import java.util.Map;
public class KillObjective implements QuestObjective {
    private final String objectiveId;
    private final String mobId;
    private final int amount;
    private final List<String> completionEvents;
    private final String description;

    public KillObjective(String objectiveId, String mobId, int amount, Map<String, String> args) {
        this.objectiveId = objectiveId;
        this.mobId = mobId;
        this.amount = amount;
        // Parse events from the arguments map
        this.completionEvents = args.containsKey("events") ? List.of(args.get("events").split(",")) : Collections.emptyList();
        // A default description, can be overridden by a 'description:' arg
        this.description = args.getOrDefault("description", "Kill " + amount + " " + mobId);
    }

    @Override
    public String getObjectiveId() { return objectiveId; }

    @Override
    public String getObjectiveType() { return "kill"; }

    @Override
    public String getDescription() { return description; } // <-- NEW

    @Override
    public List<String> getCompletionEvents() { return completionEvents; } // <-- NEW

    public String getMobId() { return mobId; }

    public int getAmount() { return amount; }
}