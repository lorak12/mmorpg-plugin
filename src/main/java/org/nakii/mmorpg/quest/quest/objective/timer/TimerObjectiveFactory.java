package org.nakii.mmorpg.quest.quest.objective.timer;

import org.nakii.mmorpg.quest.api.Objective;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.event.EventID;
import org.nakii.mmorpg.quest.api.quest.objective.ObjectiveFactory;

import java.util.List;

/**
 * Factory for creating {@link TimerObjective} instances from {@link Instruction}s.
 */
public class TimerObjectiveFactory implements ObjectiveFactory {
    /**
     * The QuestTypeAPI instance.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Constructs a new TimerObjectiveFactory.
     *
     * @param questTypeApi the QuestTypeApi instance
     */
    public TimerObjectiveFactory(final QuestTypeApi questTypeApi) {
        this.questTypeApi = questTypeApi;
    }

    @Override
    public Objective parseInstruction(final Instruction instruction) throws QuestException {
        final Variable<Number> targetAmount = instruction.getValue("amount", Argument.NUMBER, Integer.MAX_VALUE);
        final Variable<String> name = instruction.getValue("name", Argument.STRING, "");
        final Variable<Number> interval = instruction.getValue("interval", Argument.NUMBER, 1);
        final Variable<List<EventID>> doneEvents = instruction.getValueList("done", EventID::new);
        return new TimerObjective(instruction, targetAmount, questTypeApi, name, interval, doneEvents);
    }
}
