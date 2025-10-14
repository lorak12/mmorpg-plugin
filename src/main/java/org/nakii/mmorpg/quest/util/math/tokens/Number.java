package org.nakii.mmorpg.quest.util.math.tokens;

import org.nakii.mmorpg.quest.api.profile.Profile;
import org.jetbrains.annotations.Nullable;

/**
 * Token that is just any number.
 *
 * @deprecated This should be replaced with a real expression parsing lib
 */
@Deprecated
public class Number implements Token {

    /**
     * The value.
     */
    private final double value;

    /**
     * Creates a new number.
     *
     * @param value value of the number
     */
    public Number(final double value) {
        this.value = value;
    }

    @Override
    public double resolve(@Nullable final Profile profile) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
