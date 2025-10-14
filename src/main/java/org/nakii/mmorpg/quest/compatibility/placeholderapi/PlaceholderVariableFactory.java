package org.nakii.mmorpg.quest.compatibility.placeholderapi;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariableFactory;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariableFactory;
import org.nakii.mmorpg.quest.api.quest.variable.nullable.NullableVariableAdapter;

/**
 * Factory to create {@link PlaceholderVariable}s from {@link Instruction}s.
 */
public class PlaceholderVariableFactory implements PlayerVariableFactory, PlayerlessVariableFactory {

    /**
     * The empty default constructor.
     */
    public PlaceholderVariableFactory() {
    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) {
        return parseInstruction(instruction);
    }

    @Override
    public PlayerlessVariable parsePlayerless(final Instruction instruction) {
        return parseInstruction(instruction);
    }

    private NullableVariableAdapter parseInstruction(final Instruction instruction) {
        final String placeholder = String.join(".", instruction.getValueParts());
        return new NullableVariableAdapter(new PlaceholderVariable(placeholder));
    }
}
