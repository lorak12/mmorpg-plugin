package org.nakii.mmorpg.quest.quest.event.tag;

import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.database.TagData;

/**
 * The static tag event, doing what was defined in its instruction.
 */
public class PlayerlessTagEvent implements PlayerlessEvent {

    /**
     * Static tagData that shall be tagged.
     */
    private final TagData tagData;

    /**
     * Tags changer that will add or remove the defined tags.
     */
    private final TagChanger tagChanger;

    /**
     * Create a static tag event.
     *
     * @param tagData    Static tagData that shall be tagged.
     * @param tagChanger changes the defined tags
     */
    public PlayerlessTagEvent(final TagData tagData, final TagChanger tagChanger) {

        this.tagData = tagData;
        this.tagChanger = tagChanger;
    }

    @Override
    public void execute() throws QuestException {
        tagChanger.changeTags(tagData, null);
    }
}
