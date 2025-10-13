package org.nakii.mmorpg.quest.conversation;

import java.util.List;
import java.util.Map;

/**
 * Represents the static, pre-loaded "script" of a conversation from a YAML file.
 * This is a stateless data holder.
 */
public record ConversationData(
        String id,
        String quester,
        String first,
        Map<String, NPCOption> npcOptions,
        Map<String, PlayerOption> playerOptions
) {
    public List<PlayerOption> getPlayerOptions(List<String> pointers) {
        return pointers.stream()
                .map(playerOptions::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public NPCOption getNpcOption(String id) {
        return npcOptions.get(id);
    }
}