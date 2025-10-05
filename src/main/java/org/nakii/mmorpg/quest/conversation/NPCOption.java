package org.nakii.mmorpg.quest.conversation;

import java.util.List;

/**
 * A record representing a piece of dialogue spoken by an NPC.
 */
public record NPCOption(
        String id,
        String text,
        List<String> conditions,
        List<String> events,
        List<String> pointers
) {}