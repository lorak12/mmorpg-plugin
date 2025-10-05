package org.nakii.mmorpg.quest.conversation;

import java.util.List;

/**
 * A record representing a choice a player can make in a conversation.
 */
public record PlayerOption(
        String id,
        String text,
        List<String> conditions,
        List<String> events,
        String pointer // A single pointer to an NPCOption
) {}