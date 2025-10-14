package org.nakii.mmorpg.quest.quest.condition.time.real;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessConditionFactory;
import org.nakii.mmorpg.quest.quest.condition.time.TimeFrame;

/**
 * Factory to create real time conditions from {@link Instruction}s.
 */
public class RealTimeConditionFactory implements PlayerlessConditionFactory {

    /**
     * Create the real time condition factory.
     */
    public RealTimeConditionFactory() {
    }

    @Override
    public PlayerlessCondition parsePlayerless(final Instruction instruction) throws QuestException {
        final TimeFrame timeFrame = TimeFrame.parse(instruction.next());
        return new RealTimeCondition(timeFrame);
    }
}
