package org.nakii.mmorpg.quest.engine.event;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.model.QuestPackage; // <-- NEW IMPORT

/**
 * Interface for all quest events. An event is a piece of code that
 * modifies the game world or player data.
 */
@FunctionalInterface
public interface QuestEvent {
    // FIX: Added QuestPackage context to the execute method
    void execute(Player player, MMORPGCore plugin, QuestPackage context);
}