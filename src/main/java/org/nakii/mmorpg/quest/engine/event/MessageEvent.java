package org.nakii.mmorpg.quest.engine.event;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.model.QuestPackage;
import org.nakii.mmorpg.util.ChatUtils; // Assuming you have this utility

/**
 * Event that sends a formatted message to a player.
 */
public class MessageEvent implements QuestEvent {

    private final String message;

    public MessageEvent(String message) {
        this.message = message;
    }

    @Override
    public void execute(Player player, MMORPGCore plugin, QuestPackage context) { // <-- Signature changed
        String formattedMessage = message.replace("%player%", player.getName());
        player.sendMessage(ChatUtils.format(formattedMessage));
    }
}