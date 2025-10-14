package org.nakii.mmorpg.quest.quest.event.point;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.instruction.argument.Argument;
import org.nakii.mmorpg.quest.api.instruction.argument.PackageArgument;
import org.nakii.mmorpg.quest.api.instruction.variable.Variable;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEvent;
import org.nakii.mmorpg.quest.api.quest.event.PlayerlessEventFactory;
import org.nakii.mmorpg.quest.api.quest.event.nullable.NullableEventAdapter;
import org.nakii.mmorpg.quest.database.GlobalData;

import java.util.Locale;

/**
 * Factory to create global points events from {@link Instruction}s.
 */
public class GlobalPointEventFactory implements PlayerEventFactory, PlayerlessEventFactory {
    /**
     * The global data.
     */
    private final GlobalData globalData;

    /**
     * Create the global points event factory.
     *
     * @param globalData the global data
     */
    public GlobalPointEventFactory(final GlobalData globalData) {
        this.globalData = globalData;
    }

    @Override
    public PlayerEvent parsePlayer(final Instruction instruction) throws QuestException {
        return parseCombinedEvent(instruction);
    }

    @Override
    public PlayerlessEvent parsePlayerless(final Instruction instruction) throws QuestException {
        return parseCombinedEvent(instruction);
    }

    private NullableEventAdapter parseCombinedEvent(final Instruction instruction) throws QuestException {
        return new NullableEventAdapter(createGlobalPointEvent(instruction));
    }

    private GlobalPointEvent createGlobalPointEvent(final Instruction instruction) throws QuestException {
        final Variable<String> category = instruction.get(PackageArgument.IDENTIFIER);
        final String number = instruction.next();
        final String action = instruction.getValue("action");
        if (action != null) {
            try {
                final Point type = Point.valueOf(action.toUpperCase(Locale.ROOT));
                return new GlobalPointEvent(globalData, category, instruction.get(number, Argument.NUMBER), type);
            } catch (final IllegalArgumentException e) {
                throw new QuestException("Unknown modification action: " + instruction.current(), e);
            }
        }
        if (!number.isEmpty() && number.charAt(0) == '*') {
            return new GlobalPointEvent(globalData, category, instruction.get(number.replace("*", ""), Argument.NUMBER), Point.MULTIPLY);
        }
        return new GlobalPointEvent(globalData, category, instruction.get(number, Argument.NUMBER), Point.ADD);
    }
}
