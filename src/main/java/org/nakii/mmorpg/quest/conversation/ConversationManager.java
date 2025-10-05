package org.nakii.mmorpg.quest.conversation;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.model.QuestPackage;

import java.util.*;

/**
 * Manages the state of all players currently in a conversation.
 */
public class ConversationManager {

    private final MMORPGCore plugin;
    // Maps a player to their current conversation session
    private final Map<UUID, ConversationSession> activeConversations = new HashMap<>();

    public ConversationManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public boolean isInConversation(Player player) {
        return activeConversations.containsKey(player.getUniqueId());
    }

    /**
     * Starts a new conversation for a player.
     * @param player The player starting the conversation.
     * @param npc The Citizens NPC they are talking to.
     * @param conversation The conversation data model.
     */
    public void startConversation(Player player, NPC npc, Conversation conversation, QuestPackage contextPackage) {
        if (isInConversation(player)) return;

        // --- Freeze Player & Enter Conversation Mode ---
        Pig vehicle = player.getWorld().spawn(player.getLocation(), Pig.class, pig -> {
            pig.setInvisible(true);
            pig.setSilent(true);
            pig.setGravity(false);
            pig.setAI(false);
            pig.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        });
        vehicle.addPassenger(player);

        ConversationSession session = new ConversationSession(player, npc, vehicle, conversation, contextPackage);
        activeConversations.put(player.getUniqueId(), session);

        // Clear chat and display the first node
        clearChat(player);
        displayNode(session, conversation.first());
    }

    /**
     * Ends a conversation for a player, either gracefully or forcefully.
     * @param player The player to remove from the conversation.
     */
    public void endConversation(Player player) {
        ConversationSession session = activeConversations.remove(player.getUniqueId());
        if (session != null) {
            session.getVehicle().remove(); // Clean up the vehicle
            // Unfreeze the player by setting their gamemode back and forth
            GameMode gm = player.getGameMode();
            player.setGameMode(GameMode.SPECTATOR);
            player.setGameMode(gm);
        }
    }

    private void displayNode(ConversationSession session, String nodeId) {
        // Find the next NPC option to display
        NPCOption npcOption = session.getConversation().npcOptions().get(nodeId);
        if (npcOption == null) {
            endConversation(session.getPlayer());
            return;
        }

        for (String eventString : npcOption.events()) {
            plugin.getQuestManager().executeEvent(session.getPlayer(), eventString, session.getContextPackage());
        }

        // Display NPC text
        String questerName = session.getConversation().quester();
        session.getPlayer().sendMessage(plugin.getMiniMessage().deserialize("<gold>[" + questerName + "]</gold> " + npcOption.text()));

        // Execute events attached to this NPC node
        for (String eventString : npcOption.events()) {
            plugin.getQuestManager().executeEvent(session.getPlayer(), eventString, session.getContextPackage());
        }

        // Prepare and display player options
        List<PlayerOption> availablePlayerOptions = new ArrayList<>();
        for (String pointerId : npcOption.pointers()) {
            PlayerOption playerOption = session.getConversation().playerOptions().get(pointerId);
            if (playerOption != null) {
                // TODO: Add condition checking for playerOption here
                availablePlayerOptions.add(playerOption);
            }
        }
        session.setCurrentPlayerOptions(availablePlayerOptions);

        if (availablePlayerOptions.isEmpty()) {
            // If no player options, the conversation ends here
            endConversation(session.getPlayer());
        } else {
            redrawPlayerOptions(session);
        }
    }

    public void handleInput(Player player, ConversationInput input) {
        ConversationSession session = activeConversations.get(player.getUniqueId());
        if (session == null) return;

        int currentSelection = session.getCurrentSelection();
        int maxSelection = session.getCurrentPlayerOptions().size() - 1;

        switch (input) {
            case UP:
                currentSelection--;
                if (currentSelection < 0) currentSelection = maxSelection;
                session.setCurrentSelection(currentSelection);
                redrawPlayerOptions(session);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
                break;

            case DOWN:
                currentSelection++;
                if (currentSelection > maxSelection) currentSelection = 0;
                session.setCurrentSelection(currentSelection);
                redrawPlayerOptions(session);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
                break;

            case SELECT:
                PlayerOption selectedOption = session.getCurrentPlayerOptions().get(session.getCurrentSelection());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);

                // Execute events with context
                for(String eventString : selectedOption.events()) {
                    plugin.getQuestManager().executeEvent(session.getPlayer(), eventString, session.getContextPackage());
                }

                // Move to the next NPC node or end conversation
                if (selectedOption.pointer() != null && !selectedOption.pointer().isEmpty()) {
                    displayNode(session, selectedOption.pointer());
                } else {
                    endConversation(player);
                }
                break;
        }
    }

    private void redrawPlayerOptions(ConversationSession session) {
        // This is a simple redraw. A more advanced version might use packets to avoid chat flicker.
        clearChat(session.getPlayer());
        for (int i = 0; i < session.getCurrentPlayerOptions().size(); i++) {
            PlayerOption option = session.getCurrentPlayerOptions().get(i);
            String prefix = (i == session.getCurrentSelection()) ? "<yellow><b>[>]</b></yellow> " : "<gray>[ ]</gray> ";
            session.getPlayer().sendMessage(plugin.getMiniMessage().deserialize(prefix + option.text()));
        }
    }

    private void clearChat(Player player) {
        for (int i = 0; i < 20; i++) {
            player.sendMessage(" ");
        }
    }
}