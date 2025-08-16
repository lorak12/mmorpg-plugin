package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.events.PluginTimeUpdateEvent;

import java.sql.SQLException;

public class WorldTimeManager {

    private final MMORPGCore plugin;

    // The single source of truth for time in the plugin, measured in real-life seconds.
    private long totalPluginSecondsElapsed;

    // Conversion constants based on your documentation (1 plugin day = 20 real minutes)
    public static final int REAL_SECONDS_PER_PLUGIN_MINUTE = 1; // 1200 seconds / 1200 minutes
    public static final int PLUGIN_MINUTES_PER_HOUR = 60;
    public static final int PLUGIN_HOURS_PER_DAY = 24;
    public static final int PLUGIN_SECONDS_PER_DAY = PLUGIN_HOURS_PER_DAY * PLUGIN_MINUTES_PER_HOUR * REAL_SECONDS_PER_PLUGIN_MINUTE; // 24 * 60 = 1440
    public static final int PLUGIN_DAYS_PER_MONTH = 31;
    public static final int PLUGIN_MONTHS_PER_SEASON = 3;
    public static final int PLUGIN_MONTHS_PER_YEAR = 12;
    public static final int PLUGIN_DAYS_PER_YEAR = PLUGIN_MONTHS_PER_YEAR * PLUGIN_DAYS_PER_MONTH;

    public WorldTimeManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void loadTime() {
        try {
            this.totalPluginSecondsElapsed = plugin.getDatabaseManager().loadWorldTime();
            plugin.getLogger().info("World time loaded successfully (" + totalPluginSecondsElapsed + "s elapsed).");
        } catch (SQLException e) {
            this.totalPluginSecondsElapsed = 0; // Default to 0 on failure
            plugin.getLogger().severe("Could not load world time from database! Defaulting to 0.");
            e.printStackTrace();
        }
    }

    public void saveTime() {
        try {
            plugin.getDatabaseManager().saveWorldTime(this.totalPluginSecondsElapsed);
            plugin.getLogger().info("World time saved successfully.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save world time to database!");
            e.printStackTrace();
        }
    }

    public void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                totalPluginSecondsElapsed++;

                // 1. Synchronize the in-game visual time
                long currentGameTime = calculateMinecraftTime(totalPluginSecondsElapsed);
                for (World world : Bukkit.getWorlds()) {
                    world.setTime(currentGameTime);
                }

                // 2. Fire the custom event every 10 plugin minutes (10 real seconds)
                if (totalPluginSecondsElapsed % 10 == 0) {
                    PluginTimeUpdateEvent event = new PluginTimeUpdateEvent(getCurrentHour(), getCurrentMinute());
                    Bukkit.getPluginManager().callEvent(event);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // This task runs once every real-life second (20 ticks)
    }

    // --- Time Calculation & Getter Methods ---

    public int getCurrentMinute() {
        long totalMinutes = totalPluginSecondsElapsed / REAL_SECONDS_PER_PLUGIN_MINUTE;
        return (int) (totalMinutes % PLUGIN_MINUTES_PER_HOUR);
    }

    public int getCurrentHour() {
        long totalHours = (totalPluginSecondsElapsed / REAL_SECONDS_PER_PLUGIN_MINUTE) / PLUGIN_MINUTES_PER_HOUR;
        return (int) (totalHours % PLUGIN_HOURS_PER_DAY);
    }

    // You can add getDay(), getMonth(), getSeason(), getYear() here following the same calculation pattern.

    private long calculateMinecraftTime(long totalSeconds) {
        // Map our 24-hour day (1440 plugin seconds) to Minecraft's 24000 ticks
        long secondsIntoCurrentDay = totalSeconds % PLUGIN_SECONDS_PER_DAY;
        double percentOfDay = (double) secondsIntoCurrentDay / PLUGIN_SECONDS_PER_DAY;

        // Minecraft time: 0 = sunrise (6am), 6000 = noon, 12000 = sunset (6pm), 18000 = midnight
        // We offset by -6 hours to make our 00:00 align with Minecraft's midnight (18000 ticks)
        long offsetTicks = (long) (percentOfDay * 24000) - 6000;
        if (offsetTicks < 0) {
            offsetTicks += 24000;
        }
        return offsetTicks;
    }

    public long getTotalDaysElapsed() {
        return totalPluginSecondsElapsed / PLUGIN_SECONDS_PER_DAY;
    }

    public int getCurrentDayOfMonth() {
        return (int) (getTotalDaysElapsed() % PLUGIN_DAYS_PER_MONTH) + 1; // Days are 1-31
    }

    public int getCurrentMonthOfYear() {
        return (int) ((getTotalDaysElapsed() / PLUGIN_DAYS_PER_MONTH) % PLUGIN_MONTHS_PER_YEAR) + 1; // Months 1-12
    }

    public int getCurrentYear() {
        return (int) (getTotalDaysElapsed() / PLUGIN_DAYS_PER_YEAR) + 1; // Year 1, Year 2, etc.
    }

    public Season getSeason() {
        int month = getCurrentMonthOfYear();
        if (month <= 3) return Season.SPRING;
        if (month <= 6) return Season.SUMMER;
        if (month <= 9) return Season.AUTUMN;
        return Season.WINTER; // months 10, 11, 12
    }

    public String getSeasonPrefix() {
        int monthInSeason = ((getCurrentMonthOfYear() - 1) % PLUGIN_MONTHS_PER_SEASON);
        return switch (monthInSeason) {
            case 0 -> "Early "; // First month of the season
            case 2 -> "Late ";  // Third month of the season
            default -> "";      // Middle month of the season
        };
    }

    /**
     * Checks if the current date is the last day of a season.
     * Seasons are 3 months long (93 days total), so the end of a season
     * falls on month 3, 6, 9, and 12, on day 31.
     * @return True if it's the last day of any season.
     */
    public boolean isLastDayOfSeason() {
        int month = getCurrentMonthOfYear();
        int day = getCurrentDayOfMonth();

        // Check if it's the 31st day of the 3rd, 6th, 9th, or 12th month.
        return (month % PLUGIN_MONTHS_PER_SEASON == 0) && (day == PLUGIN_DAYS_PER_MONTH);
    }

    public enum Season {
        SPRING, SUMMER, AUTUMN, WINTER
    }
}