package org.nakii.mmorpg.quest.engine.condition;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.data.PlayerQuestData;

public class ReputationCondition implements QuestCondition {

    private final String faction;
    private final String operator;
    private final double value;
    private final boolean required;

    public ReputationCondition(String faction, String operator, double value, boolean required) {
        this.faction = faction;
        this.operator = operator;
        this.value = value;
        this.required = required;
    }

    @Override
    public boolean check(Player player, MMORPGCore plugin) {
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player);
        if (data == null) return !required;

        double currentRep = data.getReputation(faction);
        boolean result = switch (operator) {
            case "==" -> currentRep == value;
            case ">" -> currentRep > value;
            case "<" -> currentRep < value;
            case ">=" -> currentRep >= value;
            case "<=" -> currentRep <= value;
            default -> false;
        };

        return required ? result : !result;
    }
}