package org.nakii.mmorpg.quest.quest.event.language;

import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;

/**
 * Changes player's language.
 */
public class LanguageEvent implements PlayerEvent {

    /**
     * The language to set.
     */
    private final Variable<String> language;

    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * Create the language event.
     *
     * @param language    the language to set
     * @param dataStorage the storage providing player data
     */
    public LanguageEvent(final Variable<String> language, final PlayerDataStorage dataStorage) {
        this.language = language;
        this.dataStorage = dataStorage;
    }

    @Override
    public void execute(final Profile profile) throws QuestException {
        final String lang = language.getValue(profile);
        dataStorage.getOffline(profile).setLanguage("default".equalsIgnoreCase(lang) ? null : lang);
    }
}
