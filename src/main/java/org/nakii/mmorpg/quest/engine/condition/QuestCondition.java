package org.nakii.mmorpg.quest.engine.condition;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;

/**
 * Interface for all quest conditions. A condition is a piece of code that
 * returns true or false, used to gate content.
 */
@FunctionalInterface
public interface QuestCondition {
    boolean check(Player player, MMORPGCore plugin);
}