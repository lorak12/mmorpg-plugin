package org.nakii.mmorpg.quest.api.instruction.argument.types;

import org.nakii.mmorpg.quest.api.instruction.argument.Argument;

/**
 * Parses a string to a string.
 */
public class StringParser implements Argument<String> {

    /**
     * Creates a new parser for strings.
     */
    public StringParser() {
    }

    @Override
    public String apply(final String string) {
        return string;
    }
}
