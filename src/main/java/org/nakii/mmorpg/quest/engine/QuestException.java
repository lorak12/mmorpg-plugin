package org.nakii.mmorpg.quest.engine;

import java.io.Serial;

public class QuestException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public QuestException(final String message) {
        super(message);
    }

    public QuestException(final String message, final Throwable cause) {
        super(message, cause);
    }
}