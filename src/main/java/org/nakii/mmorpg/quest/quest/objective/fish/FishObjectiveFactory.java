package org.nakii.mmorpg.quest.quest.objective.fish;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.Item;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.argument.IdentifierArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.bukkit.Location;

/**
 * Factory for creating {@link FishObjective} instances from {@link Instruction}s.
 */
public class FishObjectiveFactory implements ObjectiveFactory {

    /**
     * Creates a new instance of the FishObjectiveFactory.
     */
    public FishObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Item> item = instruction.get(IdentifierArgument.ITEM);
        final Variable<Number> targetAmount = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ONE);

        final String loc = instruction.getValue("hookLocation");
        final String range = instruction.getValue("range");
        final boolean hookIsNotNull = loc != null && range != null;
        final Variable<Location> hookTargetLocation = hookIsNotNull ? instruction.get(loc, Argument.LOCATION) : null;
        final Variable<Number> rangeVar = hookIsNotNull ? instruction.get(range, Argument.NUMBER) : null;
        return new FishObjective(instruction, targetAmount, item, hookTargetLocation, rangeVar);
    }
}
