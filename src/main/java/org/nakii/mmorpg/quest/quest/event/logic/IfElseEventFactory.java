package org.nakii.mmorpg.quest.quest.event.logic;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.condition.ConditionID;
import org.nakii.mmorpg.quest.api.quest.event.*;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEventAdapter;

/**
 * Factory to create if-else events from {@link Instruction}s.
 */
public class IfElseEventFactory implements PlayerEventFactory, PlayerlessEventFactory {

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * The event constructor.
     *
     * @param questTypeApi the Quest Type API
     */
    public IfElseEventFactory(final QuestTypeApi questTypeApi) {
        this.questTypeApi = questTypeApi;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return createIfElseEvent(instruction);
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return createIfElseEvent(instruction);
    }

    private NullableEventAdapter createIfElseEvent(final Instruction instruction) throws QuestException {
        final Variable<ConditionID> condition = instruction.get(ConditionID::new);
        final Variable<EventID> event = instruction.get(EventID::new);
        if (!"else".equalsIgnoreCase(instruction.next())) {
            throw new QuestException("Missing 'else' keyword");
        }
        final Variable<EventID> elseEvent = instruction.get(EventID::new);
        return new NullableEventAdapter(new IfElseEvent(condition, event, elseEvent, questTypeApi));
    }
}
