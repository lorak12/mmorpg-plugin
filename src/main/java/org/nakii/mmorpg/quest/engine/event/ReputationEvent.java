package org.nakii.mmorpg.quest.engine.event;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.model.QuestPackage;

public class ReputationEvent implements QuestEvent {

    private final String faction;
    private final double amount;

    public ReputationEvent(String faction, double amount) {
        this.faction = faction;
        this.amount = amount;
    }

    @Override
    public void execute(Player player, MMORPGCore plugin, QuestPackage context) { // <-- Signature changed
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player);
        if (data != null) {
            data.addReputation(faction, amount);
        }
    }
}