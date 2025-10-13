package org.nakii.mmorpg.quest.conversation;

import java.util.List;

public record NPCOption(
        String id,
        String text,
        List<String> conditions,
        List<String> events,
        List<String> pointers
) {}
