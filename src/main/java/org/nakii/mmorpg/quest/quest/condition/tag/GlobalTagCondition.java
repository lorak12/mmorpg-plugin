package org.nakii.mmorpg.quest.quest.condition.tag;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.database.GlobalData;

/**
 * A condition that checks if a player has a certain tag.
 */
public class GlobalTagCondition implements PlayerlessCondition {

    /**
     * The global data.
     */
    private final GlobalData globalData;

    /**
     * The tag to check for.
     */
    private final Variable<String> tag;

    /**
     * Constructor for the tag condition.
     *
     * @param globalData the global data
     * @param tag        the tag to check for
     */
    public GlobalTagCondition(final GlobalData globalData, final Variable<String> tag) {
        this.globalData = globalData;
        this.tag = tag;
    }

    @Override
    public boolean check() throws QuestException {
        return globalData.hasTag(tag.getValue(null));
    }
}
