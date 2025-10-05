package org.nakii.mmorpg.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.nakii.mmorpg.util.ChatUtils;

import java.util.List;

/**
 * A wrapper class to simplify managing a single player's scoreboard.
 * This version uses a more robust method for updating lines to prevent display bugs.
 */
public class PlayerScoreboard {

    private final Scoreboard scoreboard;
    private final Objective objective;

    public PlayerScoreboard(Player player, Component title) {
        if (player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        } else {
            this.scoreboard = player.getScoreboard();
        }

        // Use a descriptive name for the objective
        this.objective = scoreboard.getObjective("mmorpg_sidebar") == null
                ? scoreboard.registerNewObjective("mmorpg_sidebar", Criteria.DUMMY, title)
                : scoreboard.getObjective("mmorpg_sidebar");

        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(this.scoreboard);
    }

    /**
     * Clears all old lines and sets the new lines for the scoreboard.
     * @param lines A list of MiniMessage-formatted strings.
     */
    public void setLines(List<String> lines) {
        // Clear previous entries to prevent ghost lines
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Use ChatColor codes as unique, non-visible identifiers for each line
        ChatColor[] colors = ChatColor.values();

        // Add new entries from bottom to top
        for (int i = 0; i < lines.size(); i++) {
            if (i >= colors.length) break; // Should not happen with < 16 lines

            String lineText = lines.get(i);
            // Each line gets a unique, invisible entry based on a color code
            String entry = colors[i].toString();

            Team team = getOrCreateTeam(entry);
            team.prefix(ChatUtils.format(lineText));

            // Set the score for the unique entry. The score determines the line's position.
            objective.getScore(entry).setScore(lines.size() - i);
        }
    }

    /**
     * Gets or creates a team uniquely identified by its entry string.
     * @param entry The unique ChatColor entry for the team.
     * @return The corresponding Team object.
     */
    private Team getOrCreateTeam(String entry) {
        Team team = scoreboard.getTeam(entry);
        if (team == null) {
            team = scoreboard.registerNewTeam(entry);
            team.addEntry(entry); // Link the team to its own unique entry
        }
        return team;
    }
}