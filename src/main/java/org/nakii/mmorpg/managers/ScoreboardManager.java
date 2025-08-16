package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.scoreboard.PlayerScoreboard;
import org.nakii.mmorpg.scoreboard.ScoreboardProvider;
import org.nakii.mmorpg.utils.ChatUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class ScoreboardManager {

    private final MMORPGCore plugin;
    private final Map<UUID, PlayerScoreboard> playerBoards = new HashMap<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private final Map<UUID, ScoreboardProvider> activeProviders = new HashMap<>();

    public ScoreboardManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void setScoreboard(Player player) {
        PlayerScoreboard board = new PlayerScoreboard(player, ChatUtils.format("<gradient:#FFFFFF:#FFFF00><b>MMORPG</b></gradient>"));
        playerBoards.put(player.getUniqueId(), board);
        updateScoreboard(player); // Initial update
    }

    public void removeScoreboard(Player player) {
        playerBoards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard()); // Reset to default
    }

    /**
     * --- NEW: Methods to control the active provider ---
     */
    public void setActiveProvider(Player player, ScoreboardProvider provider) {
        activeProviders.put(player.getUniqueId(), provider);
        updateScoreboard(player); // Update immediately when the provider changes
    }

    public void clearActiveProvider(Player player) {
        activeProviders.remove(player.getUniqueId());
        updateScoreboard(player); // Update immediately
    }

    /**
     * The main method to update a player's scoreboard with the latest data.
     */
    public void updateScoreboard(Player player) {
        if (!playerBoards.containsKey(player.getUniqueId())) return;

        PlayerScoreboard board = playerBoards.get(player.getUniqueId());
        List<String> lines = new ArrayList<>();

        // --- Static Information ---
        lines.add(" "); // Empty line

        // Date & Time (from WorldTimeManager)
        WorldTimeManager timeManager = plugin.getWorldTimeManager();

        // 1. Get Date components
        String seasonPrefix = timeManager.getSeasonPrefix();
        WorldTimeManager.Season currentSeason = timeManager.getSeason();
        int dayOfMonth = timeManager.getCurrentDayOfMonth();

        // Get the correct suffix for the day (1st, 2nd, 3rd, 4th, etc.)
        String daySuffix;
        if (dayOfMonth >= 11 && dayOfMonth <= 13) {
            daySuffix = "th";
        } else {
            daySuffix = switch (dayOfMonth % 10) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
        }

        String seasonName = currentSeason.name().substring(0, 1) + currentSeason.name().substring(1).toLowerCase();
        String dateLine = String.format("<white>%s%s %d%s</white>", seasonPrefix, seasonName, dayOfMonth, daySuffix);
        lines.add(dateLine);

        // 2. Get Time components (this logic remains the same)
        String ampm = timeManager.getCurrentHour() >= 12 ? "pm" : "am";
        int displayHour = timeManager.getCurrentHour() % 12;
        if (displayHour == 0) displayHour = 12;
        String timeIcon = (timeManager.getCurrentHour() >= 6 && timeManager.getCurrentHour() < 19) ? "☼" : "☾";
        String timeLine = String.format("<gray>%d:%02d%s %s</gray>", displayHour, timeManager.getCurrentMinute(), ampm, timeIcon);
        lines.add(timeLine);

        // Location (from ZoneManager)
//        String zone = plugin.getZoneManager().getZoneName(player.getLocation());
        String zone = "<aqua>Village</aqua>";
        lines.add(zone);
        lines.add("  "); // Empty line

        // Economy (from EconomyManager)
        double purse = plugin.getEconomyManager().getEconomy(player).getPurse();
        lines.add("<white>Purse: <gold>" + formatter.format(Math.floor(purse)) + "</gold>");
        lines.add("   "); // Empty line

        // --- Dynamic "Changing" Part ---
        ScoreboardProvider provider = activeProviders.get(player.getUniqueId());
        if (provider != null) {
            lines.addAll(provider.getScoreboardLines(player));
        }

        board.setLines(lines);
    }

    private List<String> getSlayerQuestLines(Player player) {
        var quest = plugin.getSlayerManager().getActiveQuest(player);
        var config = plugin.getSlayerManager().getSlayerConfig();
        String bossName = config.getString(quest.getSlayerType() + ".display-name");

        return List.of(
                "<white>Slayer Quest</white>",
                bossName,
                String.format("<white>(<yellow>%,.0f</yellow>/<red>%,d</red>) <gray>Combat XP</gray>",
                        quest.getCurrentXp(), quest.getXpToSpawn())
        );
    }
}