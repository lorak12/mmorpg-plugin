package org.nakii.mmorpg.quest.engine.events;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.api.QuestEvent;
import org.nakii.mmorpg.quest.engine.instruction.Instruction;
import org.nakii.mmorpg.quest.engine.profile.Profile;

public class TagEvent implements QuestEvent {

    private final String tagName;
    private final boolean add; // true to add, false to remove

    public TagEvent(Instruction instruction, boolean add) throws QuestException {
        // Instruction is "tag add <tag_name>" or "tag remove <tag_name>"
        instruction.next(); // consume "tag"
        instruction.next(); // consume "add" or "remove"
        this.tagName = instruction.current();
        this.add = add;
    }

    @Override
    public void execute(Profile profile) throws QuestException {
        if (profile == null || profile.getPlayer().getPlayer() == null) {
            throw new QuestException("Player must be online for 'tag' event.");
        }
        PlayerQuestData data = MMORPGCore.getInstance().getQuestManager().getPlayerData(profile.getPlayer().getPlayer());
        if (data != null) {
            if (add) {
                data.addTag(tagName);
            } else {
                data.removeTag(tagName);
            }
        }
    }
}