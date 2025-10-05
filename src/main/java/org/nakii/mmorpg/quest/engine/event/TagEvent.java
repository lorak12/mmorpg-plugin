package org.nakii.mmorpg.quest.engine.event;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.model.QuestPackage;

/**
 * Event that adds or removes a tag from a player.
 */
public class TagEvent implements QuestEvent {

    private final String tag;
    private final boolean add; // true to add, false to remove

    public TagEvent(String tag, boolean add) {
        this.tag = tag;
        this.add = add;
    }

    @Override
    public void execute(Player player, MMORPGCore plugin, QuestPackage context) { // <-- Signature changed
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player);
        if (data == null) return;

        if (add) {
            data.addTag(tag);
        } else {
            data.removeTag(tag);
        }
    }
}