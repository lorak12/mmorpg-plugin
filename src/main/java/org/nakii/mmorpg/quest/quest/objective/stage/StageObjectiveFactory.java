package org.nakii.mmorpg.quest.quest.objective.stage;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveID;

import java.util.List;

/**
 * Factory for creating {@link StageObjective} instances from {@link Instruction}s.
 */
public class StageObjectiveFactory implements ObjectiveFactory {

    /**
     * Creates a new instance of the StageObjectiveFactory.
     */
    public StageObjectiveFactory() {
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final List<String> stages = instruction.getList(entry -> entry).getValue(null);
        final StageObjective.StageMap stageMap = new StageObjective.StageMap(stages, (ObjectiveID) instruction.getID());
        final boolean preventCompletion = instruction.hasArgument("preventCompletion");
        return new StageObjective(instruction, stageMap, preventCompletion);
    }
}
