package org.nakii.mmorpg.quest.quest.condition.time.real;

import org.nakii.mmorpg.quest.api.instruction.Instruction;
import org.nakii.mmorpg.quest.api.logger.BetonQuestLogger;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessCondition;
import org.nakii.mmorpg.quest.api.quest.condition.PlayerlessConditionFactory;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.util.Locale;

/**
 * Factory to create day of weeks conditions from {@link Instruction}s.
 */
public class DayOfWeekConditionFactory implements PlayerlessConditionFactory {
    /**
     * Custom {@link BetonQuestLogger} instance for this class.
     */
    private final BetonQuestLogger log;

    /**
     * Create the day of week condition factory.
     *
     * @param log the logger to use when parsing the day
     */
    public DayOfWeekConditionFactory(final BetonQuestLogger log) {
        this.log = log;
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    @Override
    public PlayerlessCondition parsePlayerless(final Instruction instruction) throws QuestException {
        final String dayString = instruction.next();
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.of(Integer.parseInt(dayString));
        } catch (final DateTimeException e) {
            throw new QuestException(dayString + " is not a valid day of a week", e);
        } catch (final NumberFormatException e) {
            log.debug(instruction.getPackage(), "Could not parse number!", e);
            try {
                dayOfWeek = DayOfWeek.valueOf(dayString.toUpperCase(Locale.ROOT));
            } catch (final IllegalArgumentException iae) {
                throw new QuestException(dayString + " is not a valid day of a week", iae);
            }
        }
        return new DayOfWeekCondition(dayOfWeek);
    }
}
