package org.nakii.mmorpg.quest.engine.condition;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.util.StringUtils;

import java.util.Map;
import java.util.Optional;

public class ConditionFactory {

    private final MMORPGCore plugin;

    public ConditionFactory(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public Optional<QuestCondition> createCondition(String conditionString) {
        if (conditionString == null || conditionString.isEmpty()) {
            return Optional.empty();
        }

        boolean negate = conditionString.startsWith("!");
        if (negate) {
            conditionString = conditionString.substring(1);
        }

        String[] parts = conditionString.split(" ");
        String type = parts[0].toLowerCase();

        switch (type) {
            case "tag":
                if (parts.length >= 2) {
                    return Optional.of(new TagCondition(parts[1], !negate));
                }
                break;

            case "item":
                if (parts.length >= 2) {
                    // FIX: Corrected the method call to include the start index
                    Map<String, String> args = StringUtils.parseArguments(parts, 1);
                    String itemId = args.get("default");
                    int amount = Integer.parseInt(args.getOrDefault("amount", "1"));
                    return Optional.of(new ItemCondition(itemId, amount, !negate));
                }
                break;

            case "reputation":
                if (parts.length >= 4) {
                    String faction = parts[1];
                    String operator = parts[2];
                    double value = Double.parseDouble(parts[3]);
                    return Optional.of(new ReputationCondition(faction, operator, value, !negate));
                }
                break;
        }

        plugin.getLogger().warning("Unknown or malformed condition string: " + conditionString);
        return Optional.empty();
    }
}