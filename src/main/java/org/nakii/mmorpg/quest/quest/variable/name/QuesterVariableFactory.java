package org.nakii.mmorpg.quest.quest.variable.name;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariableFactory;

/**
 * Factory to create {@link QuesterVariable}s from {@link Instruction}s.
 */
public class QuesterVariableFactory implements PlayerVariableFactory {

    /**
     * Create a NpcName variable factory.
     */
    public QuesterVariableFactory() {
    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) {
        return new QuesterVariable();
    }
}
