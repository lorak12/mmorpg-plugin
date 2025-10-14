package org.nakii.mmorpg.quest.quest.event.scoreboard;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.OnlineProfile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.online.OnlineEvent;

/**
 * Adds or removes a scoreboard tag.
 */
public class ScoreboardTagEvent implements OnlineEvent {

    /**
     * The tag to add or remove.
     */
    private final String tag;

    /**
     * Whether to add or remove the tag.
     */
    private final Variable<ScoreboardTagAction> action;

    /**
     * Create a new scoreboard tag event that adds or removes the given tag.
     *
     * @param tag    the tag to add or remove
     * @param action whether to add or remove the tag
     */
    public ScoreboardTagEvent(final String tag, final Variable<ScoreboardTagAction> action) {
        this.tag = tag;
        this.action = action;
    }

    @Override
    public void execute(final OnlineProfile profile) throws QuestException {
        action.getValue(profile).execute(profile, tag);
    }
}
