package org.nakii.mmorpg.quest.quest.event.tag;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.database.TagData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A tag changer that will remove specified tags.
 */
public class DeleteTagChanger implements TagChanger {

    /**
     * Tags to remove from the player.
     */
    private final Variable<List<String>> tags;

    /**
     * Create the tag changer that removes tags.
     *
     * @param tags tags to remove
     */
    public DeleteTagChanger(final Variable<List<String>> tags) {
        this.tags = tags;
    }

    @Override
    public void changeTags(final TagData tagData, @Nullable final Profile profile) throws QuestException {
        for (final String tag : tags.getValue(profile)) {
            tagData.removeTag(tag);
        }
    }
}
