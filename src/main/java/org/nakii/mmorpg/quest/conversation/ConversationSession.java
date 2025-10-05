package org.nakii.mmorpg.quest.conversation;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.nakii.mmorpg.quest.model.QuestPackage;
import java.util.List;

public class ConversationSession {
    private final Player player;
    private final NPC npc;
    private final Vehicle vehicle;
    private final Conversation conversation;
    private final QuestPackage contextPackage; // <-- NEW: The package this conversation belongs to.

    private List<PlayerOption> currentPlayerOptions;
    private int currentSelection = 0;

    public ConversationSession(Player player, NPC npc, Vehicle vehicle, Conversation conversation, QuestPackage contextPackage) {
        this.player = player;
        this.npc = npc;
        this.vehicle = vehicle;
        this.conversation = conversation;
        this.contextPackage = contextPackage; // <-- NEW
    }

    // --- Getters ---
    public Player getPlayer() { return player; }
    public NPC getNpc() { return npc; }
    public Vehicle getVehicle() { return vehicle; }
    public Conversation getConversation() { return conversation; }
    public QuestPackage getContextPackage() { return contextPackage; } // <-- NEW
    public List<PlayerOption> getCurrentPlayerOptions() { return currentPlayerOptions; }
    public int getCurrentSelection() { return currentSelection; }

    // --- Setters ---
    public void setCurrentPlayerOptions(List<PlayerOption> options) { this.currentPlayerOptions = options; }
    public void setCurrentSelection(int selection) { this.currentSelection = selection; }
}