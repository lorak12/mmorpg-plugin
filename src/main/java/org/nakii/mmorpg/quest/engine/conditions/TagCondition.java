package org.nakii.mmorpg.quest.engine.conditions;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.engine.QuestException;
import org.nakii.mmorpg.quest.engine.api.QuestCondition;
import org.nakii.mmorpg.quest.engine.instruction.Instruction;
import org.nakii.mmorpg.quest.engine.profile.Profile;

public class TagCondition implements QuestCondition {

    private final String requiredTag;

    public TagCondition(Instruction instruction) throws QuestException {
        // The instruction is "tag <tag_name>", so we get the part after "tag"
        instruction.next(); // Consume "tag"
        this.requiredTag = instruction.current();
    }

    @Override
    public boolean check(Profile profile) throws QuestException {
        if (profile == null || profile.getPlayer().getPlayer() == null) {
            throw new QuestException("Player must be online for 'tag' condition.");
        }
        PlayerQuestData data = MMORPGCore.getInstance().getQuestManager().getPlayerData(profile.getPlayer().getPlayer());
        if (data == null) {
            return false; // Player data not loaded, so they can't have the tag.
        }
        return data.hasTag(requiredTag);
    }
}