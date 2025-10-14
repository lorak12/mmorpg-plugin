package org.nakii.mmorpg.quest.quest.variable.name;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariableFactory;

/**
 * Factory to create {@link PlayerNameVariable}s from {@link Instruction}s.
 */
public class PlayerNameVariableFactory implements PlayerVariableFactory {

    /**
     * Create a PlayerName variable factory.
     */
    public PlayerNameVariableFactory() {

    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) throws QuestException {
        final Variable<PlayerNameType> type;
        if (instruction.hasNext()) {
            type = instruction.get(Argument.ENUM(PlayerNameType.class));
        } else {
            type = new Variable<>(PlayerNameType.NAME);
        }
        return new PlayerNameVariable(type);
    }
}
