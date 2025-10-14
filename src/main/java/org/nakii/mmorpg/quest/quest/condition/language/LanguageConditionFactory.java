package org.nakii.mmorpg.quest.quest.condition.language;

import org.nakii.mmorpg.quest.api.LanguageProvider;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerConditionFactory;
import org.nakii.mmorpg.quest.config.PluginMessage;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;

import java.util.List;

/**
 * Factory for {@link LanguageCondition}s.
 */
public class LanguageConditionFactory implements PlayerConditionFactory {

    /**
     * Storage for required player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * The language provider to get the default language.
     */
    private final LanguageProvider languageProvider;

    /**
     * The {@link PluginMessage} instance.
     */
    private final PluginMessage pluginMessage;

    /**
     * Create a language condition factory.
     *
     * @param dataStorage      the storage for used player data
     * @param languageProvider the language provider to get the default language
     * @param pluginMessage    the {@link PluginMessage} instance
     */
    public LanguageConditionFactory(final PlayerDataStorage dataStorage, final LanguageProvider languageProvider,
                                    final PluginMessage pluginMessage) {
        this.dataStorage = dataStorage;
        this.languageProvider = languageProvider;
        this.pluginMessage = pluginMessage;
    }

    @Override
    public PlayerCondition parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<List<String>> languages = instruction.getList(Argument.STRING, list -> {
            for (final String language : list) {
                if (!pluginMessage.getLanguages().contains(language)) {
                    throw new QuestException("Language " + language + " does not exist.");
                }
            }
        });
        return new LanguageCondition(dataStorage, languageProvider, languages);
    }
}
