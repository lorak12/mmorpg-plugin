package org.nakii.mmorpg.quest.quest.objective.enchant;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.argument.IdentifierArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.instruction.variable.VariableList;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

import java.util.List;

/**
 * Factory for creating {@link EnchantObjective} instances from {@link Instruction}s.
 */
public class EnchantObjectiveFactory implements ObjectiveFactory {
    /**
     * The one keyword for the requirement mode.
     */
    private static final String JUST_ONE_ENCHANT = "one";

    /**
     * Creates a new instance of the EnchantObjectiveFactory.
     */
    public EnchantObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Number> targetAmount = instruction.getValue("amount", Argument.NUMBER_NOT_LESS_THAN_ONE, 1);
        final Variable<Item> item = instruction.get(IdentifierArgument.ITEM);
        final Variable<List<EnchantObjective.EnchantmentData>> desiredEnchantments =
                instruction.getList(EnchantObjective.EnchantmentData::convert, VariableList.notEmptyChecker());
        final boolean requireOne = JUST_ONE_ENCHANT.equalsIgnoreCase(instruction.getValue("requirementMode"));
        return new EnchantObjective(instruction, targetAmount, item, desiredEnchantments, requireOne);
    }
}
