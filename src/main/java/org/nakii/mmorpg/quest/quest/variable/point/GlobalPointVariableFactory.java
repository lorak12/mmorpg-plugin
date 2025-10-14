package org.nakii.mmorpg.quest.quest.variable.point;

import org.apache.commons.lang3.tuple.Triple;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerlessVariableFactory;
import org.nakii.mmorpg.quest.database.GlobalData;

/**
 * A factory for creating Global Point variables.
 */
public class GlobalPointVariableFactory extends AbstractPointVariableFactory<GlobalData> implements PlayerlessVariableFactory {

    /**
     * Create a new Point variable factory.
     *
     * @param globalData the global data holder
     * @param logger     the logger instance for this factory
     */
    public GlobalPointVariableFactory(final GlobalData globalData, final BetonQuestLogger logger) {
        super(globalData, logger);
    }

    @Override
    public PlayerlessVariable parsePlayerless(final Instruction instruction) throws QuestException {
        final Triple<String, Integer, PointCalculationType> values = parseInstruction(instruction);
        return new GlobalPointVariable(dataHolder, values.getLeft(), values.getMiddle(), values.getRight());
    }
}
