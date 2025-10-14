package org.nakii.mmorpg.quest.quest.variable.tag;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackage;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariable;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.database.GlobalData;

/**
 * Exposes the presence of global tags as a variable.
 * Originally implemented for use with the PAPI integration.
 */
public class GlobalTagVariable extends AbstractTagVariable<GlobalData> implements PlayerlessVariable {
    /**
     * Constructs a new GlobalTagVariable.
     *
     * @param pluginMessage the {@link PluginMessage} instance
     * @param data          the data holder
     * @param tagName       the tag to check for
     * @param questPackage  the quest package to check for the tag
     * @param papiMode      whether to return true/false or the configured messages
     */
    public GlobalTagVariable(final PluginMessage pluginMessage, final GlobalData data, final String tagName, final QuestPackage questPackage, final boolean papiMode) {
        super(pluginMessage, data, tagName, questPackage, papiMode);
    }

    @Override
    public String getValue() throws QuestException {
        return getValueFor(null, data.getTags());
    }
}
