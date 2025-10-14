package org.nakii.mmorpg.quest.quest.objective.action;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.nakii.mmorpg.quest.util.BlockSelector;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Factory class for creating {@link ActionObjective} instances from {@link Instruction}s.
 */
public class ActionObjectiveFactory implements ObjectiveFactory {
    /**
     * The "any" keyword.
     */
    private static final String ANY = "any";

    /**
     * Creates a new instance of the ActionObjectiveFactory.
     */
    public ActionObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Click> action = instruction.get(Argument.ENUM(Click.class));
        final Variable<BlockSelector> selector;
        if (ANY.equalsIgnoreCase(instruction.next())) {
            selector = null;
        } else {
            selector = instruction.get(instruction.current(), Argument.BLOCK_SELECTOR);
        }
        final boolean exactMatch = instruction.hasArgument("exactMatch");
        final Variable<Location> loc = instruction.getValue("loc", Argument.LOCATION);
        final Variable<Number> range = instruction.getValue("range", Argument.NUMBER, 0);
        final boolean cancel = instruction.hasArgument("cancel");
        final String handString = instruction.getValue("hand");
        final EquipmentSlot slot;
        if (handString == null || handString.equalsIgnoreCase(EquipmentSlot.HAND.toString())) {
            slot = EquipmentSlot.HAND;
        } else if (handString.equalsIgnoreCase(EquipmentSlot.OFF_HAND.toString())) {
            slot = EquipmentSlot.OFF_HAND;
        } else if (ANY.equalsIgnoreCase(handString)) {
            slot = null;
        } else {
            throw new QuestException("Invalid hand value: " + handString);
        }
        return new ActionObjective(instruction, action, selector, exactMatch, loc, range, cancel, slot);
    }
}
