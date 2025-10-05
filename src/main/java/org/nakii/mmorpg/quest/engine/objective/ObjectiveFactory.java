package org.nakii.mmorpg.quest.engine;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.engine.objective.KillObjective;
import org.nakii.mmorpg.quest.engine.objective.QuestObjective;
import org.nakii.mmorpg.util.StringUtils;

import java.util.Map;
import java.util.Optional;

public class ObjectiveFactory {

    private final MMORPGCore plugin;

    public ObjectiveFactory(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public Optional<QuestObjective> createTemplate(String objectiveId, String objectiveString) {
        String[] parts = objectiveString.split(" ");
        String type = parts[0].toLowerCase();

        // FIX: Parse all arguments from the string
        Map<String, String> args = StringUtils.parseArguments(parts, 1);

        switch (type) {
            case "kill":
                if (parts.length >= 3) {
                    String mobId = parts[1];
                    int amount = Integer.parseInt(parts[2]);
                    // Pass the full arguments map to the constructor
                    return Optional.of(new KillObjective(objectiveId, mobId, amount, args));
                }
                break;
        }
        return Optional.empty();
    }
}