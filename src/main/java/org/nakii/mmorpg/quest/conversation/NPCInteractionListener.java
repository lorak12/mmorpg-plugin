package org.nakii.mmorpg.quest.conversation;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestManager;
import org.nakii.mmorpg.quest.data.ActiveObjective;
import org.nakii.mmorpg.quest.data.PlayerQuestData;
import org.nakii.mmorpg.quest.engine.objective.InteractObjective;
import org.nakii.mmorpg.quest.engine.objective.QuestObjective;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class NPCInteractionListener implements Listener {

    private final MMORPGCore plugin;

    public NPCInteractionListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        NPC npc = event.getNPC();
        QuestManager questManager = plugin.getQuestManager();
        ConversationManager conversationManager = plugin.getConversationManager();

        // --- Complete any InteractObjectives first ---
        PlayerQuestData data = questManager.getPlayerData(player);
        if (data != null && !data.getActiveObjectives().isEmpty()) {
            Iterator<ActiveObjective> iterator = data.getActiveObjectives().iterator();
            while (iterator.hasNext()) {
                ActiveObjective activeObjective = iterator.next();
                QuestObjective template = activeObjective.getTemplate();

                if (template instanceof InteractObjective interactObjective) {
                    if (interactObjective.getNpcId() == npc.getId()) {
                        iterator.remove();
                        plugin.getHUDManager().updateActionBar(player,
                                "<green>Objective Complete: " + interactObjective.getDescription(), 3);

                        // Execute completion events
                        List<String> completionEvents = interactObjective.getCompletionEvents();
                        for (String eventString : completionEvents) {
                            questManager.executeEvent(player, eventString,
                                    questManager.findPackageForObjective(template.getObjectiveId()));
                        }
                    }
                }
            }
        }

        // --- Find and start the conversation for this NPC ---
        Optional<QuestManager.ConversationContext> contextOpt = questManager.findConversationForNPC(npc.getId());
        contextOpt.ifPresent(context -> {
            if (!conversationManager.isInConversation(player)) {
                conversationManager.startConversation(player, npc,
                        context.conversation(), context.questPackage());
            }
        });
    }
}
