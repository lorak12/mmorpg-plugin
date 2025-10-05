package org.nakii.mmorpg.listeners;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestManager;

import java.util.Optional;

public class NPCInteractionListener implements Listener {

    private final MMORPGCore plugin;

    public NPCInteractionListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();

        // Find a conversation and its parent package for this NPC's ID
        Optional<QuestManager.ConversationContext> context = plugin.getQuestManager().findConversationForNPC(npc.getId());

        context.ifPresent(ctx -> {
            // Start the conversation, now with the package context
            plugin.getConversationManager().startConversation(event.getClicker(), npc, ctx.conversation(), ctx.questPackage());
        });
    }
}