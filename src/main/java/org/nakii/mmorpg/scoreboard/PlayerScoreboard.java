package org.nakii.mmorpg.scoreboard;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.List;

/**
 * A wrapper class to simplify managing a single player's scoreboard.
 */
public class PlayerScoreboard {

    private final Scoreboard scoreboard;
    private final Objective objective;

    public PlayerScoreboard(Player player, Component title) {
        // Create a new scoreboard for this specific player to avoid conflicts.
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("mmorpg_sidebar", Criteria.DUMMY, title);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(this.scoreboard);
    }

    /**
     * Clears all old lines and sets the new lines for the scoreboard.
     * @param lines A list of MiniMessage-formatted strings.
     */
    public void setLines(List<String> lines) {
        // Clear previous entries
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Add new entries from bottom to top
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // We use a Team to hold the line's content, allowing for longer text
            // than the 16-character limit of a raw score entry.
            Team team = getOrCreateTeam("line" + i);
            team.prefix(ChatUtils.format(line));

            // The entry is a "hidden" string that connects the score to the team.
            String entry = "§" + Integer.toHexString(i) + "§r";
            objective.getScore(entry).setScore(lines.size() - i);
        }
    }

    private Team getOrCreateTeam(String name) {
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
            // The "entry" is the hidden string we use to link the score to the team.
            String entry = "§" + Integer.toHexString(scoreboard.getTeams().size() - 1) + "§r";
            team.addEntry(entry);
        }
        return team;
    }
}