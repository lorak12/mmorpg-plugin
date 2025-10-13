package org.nakii.mmorpg.quest.engine.kernel.processor;

import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.api.QuestCondition;
import org.nakii.mmorpg.quest.engine.identifier.ConditionID;
import org.nakii.mmorpg.quest.engine.profile.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConditionProcessor {
    // This map will store the actual, created Condition objects.
    private final Map<String, QuestCondition> conditions = new HashMap<>();

    public void addCondition(String id, QuestCondition condition) {
        conditions.put(id.toLowerCase(), condition);
    }

    public void clear() {
        conditions.clear();
    }

    public boolean check(Profile profile, ConditionID conditionID) {
        QuestCondition condition = conditions.get(conditionID.getFull().toLowerCase());
        if (condition == null) {
            System.err.println("Warning: Unknown condition '" + conditionID.getFull() + "'");
            return false;
        }
        try {
            boolean result = condition.check(profile);
            return conditionID.isInverted() != result; // XOR for inversion
        } catch (QuestException e) {
            System.err.println("Error checking condition '" + conditionID.getFull() + "': " + e.getMessage());
            return false;
        }
    }
}