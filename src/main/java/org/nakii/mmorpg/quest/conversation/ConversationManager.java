package org.nakii.mmorpg.quest.conversation;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.model.QuestPackage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConversationManager {

    private final MMORPGCore plugin;
    private final Map<UUID, Conversation> activeConversations = new HashMap<>();

    public ConversationManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public boolean isInConversation(Player player) {
        return activeConversations.containsKey(player.getUniqueId());
    }

    public void startConversation(Player player, NPC npc, ConversationData data, QuestPackage contextPackage) {
        if (isInConversation(player)) return;

        Conversation conversation = new Conversation(plugin, this, player, npc, data, contextPackage);
        activeConversations.put(player.getUniqueId(), conversation);
        conversation.start();
    }

    public void endConversation(Player player, boolean requestedByPlayer) {
        Conversation conversation = activeConversations.get(player.getUniqueId());
        if (conversation != null) {
            conversation.end(requestedByPlayer);
        }
    }

    // Called by the Conversation object when it's done cleaning up.
    public void onConversationEnd(Player player) {
        activeConversations.remove(player.getUniqueId());
    }
}