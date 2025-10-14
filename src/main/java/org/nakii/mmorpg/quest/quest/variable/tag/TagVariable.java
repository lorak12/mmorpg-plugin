package org.nakii.mmorpg.quest.quest.variable.tag;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;

/**
 * Exposes the presence of tags as a variable.
 * Originally implemented for use with the PAPI integration.
 */
public class TagVariable extends AbstractTagVariable<PlayerDataStorage> implements PlayerVariable {

    /**
     * Constructs a new TagVariable.
     *
     * @param pluginMessage the {@link PluginMessage} instance
     * @param data          the data holder
     * @param tagName       the name of the tag
     * @param questPackage  the quest package
     * @param papiMode      whether PAPI mode is enabled
     */
    public TagVariable(final PluginMessage pluginMessage, final PlayerDataStorage data, final String tagName, final QuestPackage questPackage, final boolean papiMode) {
        super(pluginMessage, data, tagName, questPackage, papiMode);
    }

    @Override
    public String getValue(final Profile profile) throws QuestException {
        return getValueFor(profile, data.get(profile).getTags());
    }
}
