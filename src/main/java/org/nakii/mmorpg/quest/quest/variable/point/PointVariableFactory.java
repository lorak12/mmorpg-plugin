package org.nakii.mmorpg.quest.quest.variable.point;

import org.apache.commons.lang3.tuple.Triple;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariable;
import org.nakii.mmorpg.quest.api.quest.variable.PlayerVariableFactory;
import org.nakii.mmorpg.quest.data.PlayerDataStorage;

/**
 * A factory for creating Point variables.
 */
public class PointVariableFactory extends AbstractPointVariableFactory<PlayerDataStorage> implements PlayerVariableFactory {

    /**
     * Create a new Point variable factory.
     *
     * @param dataStorage the player data storage
     * @param logger      the logger instance for this factory
     */
    public PointVariableFactory(final PlayerDataStorage dataStorage, final BetonQuestLogger logger) {
        super(dataStorage, logger);
    }

    @Override
    public PlayerVariable parsePlayer(final Instruction instruction) throws QuestException {
        final Triple<String, Integer, PointCalculationType> values = parseInstruction(instruction);
        return new PointVariable(dataHolder, values.getLeft(), values.getMiddle(), values.getRight());
    }
}
