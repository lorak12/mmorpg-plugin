package org.nakii.mmorpg.quest.conversation;

import java.util.Map;

/**
 * A record representing a complete conversation tree for an NPC.
 */
public record Conversation(
        String id,
        String quester,
        String first,
        Map<String, NPCOption> npcOptions,
        Map<String, PlayerOption> playerOptions
) {}