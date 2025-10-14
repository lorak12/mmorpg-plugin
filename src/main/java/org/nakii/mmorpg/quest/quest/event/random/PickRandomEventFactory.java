package org.nakii.mmorpg.quest.quest.event.random;

import org.nakii.mmorpg.quest.api.config.quest.QuestPackageManager;
import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.QuestTypeApi;
import org.nakii.mmorpg.quest.api.quest.event.*;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEventAdapter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates new {@link PickRandomEvent} instances from an {@link Instruction}.
 */
public class PickRandomEventFactory implements PlayerEventFactory, PlayerlessEventFactory {
    /**
     * The character used to separate the percentage and event in the instruction.
     */
    private static final Pattern EVENT_WEIGHT = Pattern.compile("(?<weight>\\d+\\.?\\d?)~(?<event>.+)");

    /**
     * The quest package manager to get quest packages from.
     */
    private final QuestPackageManager packManager;

    /**
     * Quest Type API.
     */
    private final QuestTypeApi questTypeApi;

    /**
     * Creates the PickRandomEventFactory.
     *
     * @param packManager  the quest package manager to get quest packages from
     * @param questTypeApi the Quest Type API
     */
    public PickRandomEventFactory(final QuestPackageManager packManager, final QuestTypeApi questTypeApi) {
        this.packManager = packManager;
        this.questTypeApi = questTypeApi;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return createPickRandomEvent(instruction);
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return createPickRandomEvent(instruction);
    }

    private NullableEventAdapter createPickRandomEvent(final Instruction instruction) throws QuestException {
        final Variable<List<RandomEvent>> events = instruction.getList(string -> {
            final Matcher matcher = EVENT_WEIGHT.matcher(string);
            if (!matcher.matches()) {
                throw new QuestException("Weight must be specified correctly: " + string);
            }

            final String weightString = matcher.group("weight");
            final String eventString = matcher.group("event");
            final EventID eventID = new EventID(packManager, instruction.getPackage(), eventString);
            final double weight = Argument.NUMBER.apply(weightString).doubleValue();
            return new RandomEvent(eventID, weight);
        });
        final Variable<Number> amount = instruction.getValue("amount", Argument.NUMBER);
        return new NullableEventAdapter(new PickRandomEvent(events, amount, questTypeApi));
    }
}
