package org.nakii.mmorpg.quest.quest.objective.kill;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.argument.PackageArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.nakii.mmorpg.quest.util.QuestMobType; // ---> ADD THIS IMPORT

import java.util.List;

public class MobKillObjectiveFactory implements ObjectiveFactory {

    public MobKillObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        // ---> MODIFIED LINE: Use our custom parser <---
        final Variable<List<QuestMobType>> entities = instruction.getList(QuestMobType.MobTypeArgument.MOB_TYPE);

        final Variable<Number> targetAmount = instruction.get(Argument.NUMBER_NOT_LESS_THAN_ONE);
        final Variable<String> name = instruction.getValue("name", Argument.STRING);
        final Variable<String> marked = instruction.getValue("marked", PackageArgument.IDENTIFIER);
        return new MobKillObjective(instruction, targetAmount, entities, name, marked);
    }
}