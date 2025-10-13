package org.nakii.mmorpg.quest.conversation;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestManager;
import org.nakii.mmorpg.quest.conversation.io.MenuConversationIO;
import org.nakii.mmorpg.quest.conversation.io.Scroll;
import org.nakii.mmorpg.quest.engine.api.QuestApi;
import org.nakii.mmorpg.quest.engine.identifier.EventID;
import org.nakii.mmorpg.quest.engine.profile.Profile;
import org.nakii.mmorpg.quest.model.QuestPackage;

import java.util.List;
import java.util.stream.Collectors;

public class Conversation implements Listener {

    private final MMORPGCore plugin;
    private final ConversationManager conversationManager;
    private final QuestApi questApi;

    private final Profile profile;
    private final Player player;
    private final NPC npc;
    private final ConversationData data;
    private final QuestPackage contextPackage;

    private final MenuConversationIO io;
    private ConversationState state = ConversationState.CREATED;

    private List<PlayerOption> currentPlayerOptions;
    private int currentSelection;

    public Conversation(MMORPGCore plugin, ConversationManager conversationManager, Player player, NPC npc, ConversationData data, QuestPackage contextPackage) {
        this.plugin = plugin;
        this.conversationManager = conversationManager;
        this.questApi = plugin.getCoreRegistry();
        this.player = player;
        this.profile = plugin.getProfileManager().getProfile(player);
        this.npc = npc;
        this.data = data;
        this.contextPackage = contextPackage;

        // The conversation engine creates and controls its own UI
        this.io = new MenuConversationIO(plugin, this);
    }

    public void start() {
        if (state != ConversationState.CREATED) return;
        state = ConversationState.ACTIVE;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        io.start();
        displayNode(data.first());
    }

    public void end(boolean requestedByPlayer) {
        if (state == ConversationState.ENDED) return;
        state = ConversationState.ENDED;
        io.end();
        PlayerQuitEvent.getHandlerList().unregister(this); // Unregister self
        conversationManager.onConversationEnd(player);
    }

    public void handlePlayerInput(Scroll scroll) {
        if (currentPlayerOptions == null || currentPlayerOptions.isEmpty()) return;

        int max = currentPlayerOptions.size() - 1;
        switch (scroll) {
            case UP -> {
                currentSelection--;
                if (currentSelection < 0) currentSelection = max;
            }
            case DOWN -> {
                currentSelection++;
                if (currentSelection > max) currentSelection = 0;
            }
        }
        io.redrawScreen(data.quester(), getCurrentNpcText(), currentPlayerOptions, currentSelection);
    }

    public void passPlayerAnswer() {
        if (currentPlayerOptions == null || currentPlayerOptions.isEmpty()) return;

        PlayerOption selected = currentPlayerOptions.get(currentSelection);

        // Fire events using the new engine
        for (String eventString : selected.events()) {
            questApi.fireEvent(profile, new EventID(contextPackage, eventString));
        }

        // Move to next node or end conversation
        if (selected.pointer() != null && !selected.pointer().isEmpty()) {
            displayNode(selected.pointer());
        } else {
            conversationManager.endConversation(player, false);
        }
    }

    private void displayNode(String nodeId) {
        NPCOption npcOption = data.getNpcOption(nodeId);
        if (npcOption == null) {
            conversationManager.endConversation(player, false);
            return;
        }

        // Fire NPC events using the new engine
        for (String eventString : npcOption.events()) {
            questApi.fireEvent(profile, new EventID(contextPackage, eventString));
        }

        // Filter player options based on their conditions
        List<PlayerOption> allPossibleOptions = data.getPlayerOptions(npcOption.pointers());
        this.currentPlayerOptions = allPossibleOptions.stream()
                .filter(option -> plugin.getQuestManager().checkConditions(player, option.conditions()))
                .collect(Collectors.toList());

        this.currentSelection = 0;

        if (this.currentPlayerOptions.isEmpty()) {
            io.displayAndEnd(data.quester(), npcOption.text());
        } else {
            io.redrawScreen(data.quester(), npcOption.text(), currentPlayerOptions, currentSelection);
        }
    }

    private String getCurrentNpcText() {
        // This is a helper to get the current NPC text again for redrawing.
        // In a more complex system, you might store the current NPCOption.
        return "Not implemented yet"; // We'll fix this in the MenuConversationIO refactor.
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) {
            end(false);
        }
    }

    // Getters for MenuConversationIO
    public Player getPlayer() { return player; }
    public MMORPGCore getPlugin() { return plugin; }
}