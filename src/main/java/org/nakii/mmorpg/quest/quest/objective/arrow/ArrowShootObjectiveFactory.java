package org.nakii.mmorpg.quest.quest.objective.arrow;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.bukkit.Location;

/**
 * Factory for creating {@link ArrowShootObjective} instances from {@link Instruction}s.
 */
public class ArrowShootObjectiveFactory implements ObjectiveFactory {

    /**
     * Creates a new instance of the ArrowShootObjectiveFactory.
     */
    public ArrowShootObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Location> location = instruction.get(Argument.LOCATION);
        final Variable<Number> range = instruction.get(Argument.NUMBER);
        return new ArrowShootObjective(instruction, location, range);
    }
}
