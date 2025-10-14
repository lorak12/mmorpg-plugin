package org.nakii.mmorpg.quest.quest.event.language;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;

/**
 * Factory to create language events from {@link Instruction}s.
 */
public class LanguageEventFactory implements PlayerEventFactory {

    /**
     * Storage for player data.
     */
    private final PlayerDataStorage dataStorage;

    /**
     * Create the language event factory.
     *
     * @param dataStorage the storage providing player data
     */
    public LanguageEventFactory(final PlayerDataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<String> language = instruction.get(Argument.STRING);
        return new LanguageEvent(language, dataStorage);
    }
}
