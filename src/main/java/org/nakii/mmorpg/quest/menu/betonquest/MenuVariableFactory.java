package org.nakii.mmorpg.quest.menu.betonquest;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariableFactory;
import org.nakii.mmorpg.quest.api.quest.variable.online.OnlineVariableAdapter;

/**
 * Factory to create {@link MenuVariable}s from {@link Instruction}s.
 */
public class MenuVariableFactory implements PlayerVariableFactory {

    /**
     * The empty default constructor.
     */
    public MenuVariableFactory() {
    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) {
        return new OnlineVariableAdapter(new MenuVariable(), profile -> "");
    }
}
