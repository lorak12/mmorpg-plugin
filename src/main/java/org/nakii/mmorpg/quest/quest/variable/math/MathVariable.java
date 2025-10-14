package org.nakii.mmorpg.quest.quest.variable.math;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.nakii.mmorpg.quest.api.quest.QuestException;
import org.nakii.mmorpg.quest.api.quest.variable.nullable.NullableVariable;
import org.nakii.mmorpg.quest.util.math.tokens.Token;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * This variable evaluates the given calculation and returns the result.
 */
public class MathVariable implements NullableVariable {
    /**
     * The full calculation token.
     */
    @SuppressWarnings("deprecation")
    private final Token calculation;

    /**
     * Create a math variable from the given calculation.
     *
     * @param calculation calculation to parse
     */
    @SuppressWarnings("deprecation")
    public MathVariable(final Token calculation) {
        this.calculation = calculation;
    }

    @Override
    public String getValue(@Nullable final Profile profile) throws QuestException {
        final double value;
        try {
            value = this.calculation.resolve(profile);
        } catch (final QuestException e) {
            throw new QuestException("Error while resolving math variable: " + e.getMessage(), e);
        }
        if (value % 1 == 0) {
            return String.format(Locale.US, "%.0f", value);
        }
        return String.valueOf(value);
    }
}
