package org.nakii.mmorpg.quest.conversation;

import java.util.List;

public record PlayerOption(
        String id,
        String text,
        List<String> conditions,
        List<String> events,
        String pointer
) {}
