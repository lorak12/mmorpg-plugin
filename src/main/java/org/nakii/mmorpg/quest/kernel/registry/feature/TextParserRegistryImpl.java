package org.nakii.mmorpg.quest.kernel.registry.feature;

import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.text.TextParser;
import org.nakii.mmorpg.quest.api.text.TextParserRegistry;
import org.nakii.mmorpg.quest.kernel.registry.FactoryRegistry;

/**
 * A registry for text parsers.
 */
public class TextParserRegistryImpl extends FactoryRegistry<TextParser> implements TextParserRegistry {
    /**
     * Create a new type registry.
     *
     * @param log the logger that will be used for logging
     */
    public TextParserRegistryImpl(final BetonQuestLogger log) {
        super(log, "TextParser");
    }

    @Override
    public TextParser get(final String name) throws QuestException {
        return getFactory(name);
    }
}
