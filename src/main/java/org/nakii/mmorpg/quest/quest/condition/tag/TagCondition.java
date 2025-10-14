package org.nakii.mmorpg.quest.quest.condition.tag;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;

/**
 * A condition that checks if a player has a certain tag.
 */
public class TagCondition implements PlayerCondition {

    /**
     * The tag to check for.
     */
    private final Variable<String> tag;

    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * Constructor for the tag condition.
     *
     * @param tag         the tag to check for
     * @param dataStorage the storage providing player data
     */
    public TagCondition(final Variable<String> tag, final PlayerDataStorage dataStorage) {
        this.tag = tag;
        this.dataStorage = dataStorage;
    }

    @Override
    public boolean check(final Profile profile) throws QuestException {
        return dataStorage.get(profile).hasTag(tag.getValue(profile));
    }
}
