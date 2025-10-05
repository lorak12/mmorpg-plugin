package org.nakii.mmorpg.quest.engine.condition;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.data.PlayerQuestData;

/**
 * Condition that checks if a player has a specific tag.
 */
public class TagCondition implements QuestCondition {

    private final String tag;
    private final boolean required; // true if tag must be present, false if it must be absent

    public TagCondition(String tag, boolean required) {
        this.tag = tag;
        this.required = required;
    }

    @Override
    public boolean check(Player player, MMORPGCore plugin) {
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player);
        if (data == null) return false;

        boolean hasTag = data.hasTag(tag);
        return required ? hasTag : !hasTag; // If required is true, return hasTag. If required is false, return NOT hasTag.
    }
}