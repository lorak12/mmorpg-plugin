package org.nakii.mmorpg.scoreboard;

import org.bukkit.entity.Player;
import java.util.List;

/**
 * An interface for any system (Slayers, Quests, Dungeons) that wants to
 * provide dynamic, contextual lines for the scoreboard's objective section.
 */
@FunctionalInterface
public interface ScoreboardProvider {

    /**
     * Gets the list of MiniMessage-formatted strings to display on the scoreboard.
     * @param player The player for whom the lines are being generated.
     * @return A list of strings for the objective section.
     */
    List<String> getScoreboardLines(Player player);
}