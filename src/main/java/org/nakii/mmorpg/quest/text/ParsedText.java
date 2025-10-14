package org.nakii.mmorpg.quest.text;

import net.kyori.adventure.text.Component;
import org.nakii.mmorpg.quest.api.LanguageProvider;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.text.Text;
import org.nakii.mmorpg.quest.api.text.TextParser;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A text that is parsed using a text parser.
 */
public class ParsedText implements Text {
    /**
     * The text parser to use for parsing text.
     */
    private final TextParser parser;

    /**
     * The text to use for each language.
     */
    private final Map<String, Variable<String>> texts;

    /**
     * The data storage to use for getting the player's language.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * The language provider to get the default language.
     */
    private final LanguageProvider languageProvider;

    /**
     * Constructs a new parsed text with text in multiple languages.
     *
     * @param parser           the text parser to use
     * @param texts            the text to use for each language
     * @param dataStorage      the data storage to use for getting the player's language
     * @param languageProvider the language provider to get the default language
     * @throws QuestException if an error occurs while constructing the text
     */
    public ParsedText(final TextParser parser, final Map<String, Variable<String>> texts,
                      final PlayerDataStorage dataStorage, final LanguageProvider languageProvider) throws QuestException {
        this.parser = parser;
        this.texts = texts;
        this.dataStorage = dataStorage;
        this.languageProvider = languageProvider;
        if (!texts.containsKey(languageProvider.getDefaultLanguage())) {
            throw new QuestException("No text in default language defined.");
        }
    }

    @Override
    public Component asComponent(@Nullable final Profile profile) throws QuestException {
        String language = null;
        Variable<String> text = null;
        if (profile != null) {
            language = dataStorage.get(profile).getLanguage().orElseGet(languageProvider::getDefaultLanguage);
            text = texts.get(language);
        }
        if (text == null) {
            language = languageProvider.getDefaultLanguage();
            text = texts.get(language);
        }
        if (text == null) {
            throw new QuestException("No text in language " + language + " defined.");
        }
        return parser.parse(text.getValue(profile));
    }
}
