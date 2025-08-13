package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerState;
import org.nakii.mmorpg.player.PlayerStats;

/**
 * Manages the effect of environmental zones on players.
 * This class runs a periodic task to update player states based on their location.
 */
public class EnvironmentManager {

    private final MMORPGCore plugin;

    // --- CONFIGURABLE CONSTANTS ---
    // Moved hardcoded "magic numbers" to clearly named final variables.
    private static final double NATURAL_REDUCTION_RATE = 5.0;

    public EnvironmentManager(MMORPGCore plugin) {
        this.plugin = plugin;
        // In your main onEnable, you would start a BukkitRunnable that calls this for all online players every second.
        // new BukkitRunnable() { @Override public void run() { Bukkit.getOnlinePlayers().forEach(EnvironmentManager.this::updatePlayerEnvironment); } }.runTaskTimer(plugin, 20L, 20L);
    }

    /**
     * Starts the repeating task that updates all online players' environmental status.
     * This should be called once when the plugin enables.
     */
    public void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerEnvironment(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Runs once every second
    }

    /**
     * The main update tick for a single player's environmental status.
     * This method orchestrates the process of checking zones and applying effects.
     * @param player The player to update.
     */
    public void updatePlayerEnvironment(Player player) {
        ZoneManager zoneManager = plugin.getZoneManager();
        String zoneType = zoneManager.getZoneEnvironmentType(player.getLocation());

        switch (zoneType) {
            case "hot" -> handleHotZone(player);
            case "cold" -> handleColdZone(player);
            default -> handleNaturalZone(player); // "natural" or any other type
        }

        // After updating the values, a separate method applies the gameplay effects.
        applyPenalties(player);
    }

    /**
     * Handles the logic for a player inside a "hot" zone.
     */
    private void handleHotZone(Player player) {
        PlayerStateManager stateManager = plugin.getPlayerStateManager();
        ZoneManager zoneManager = plugin.getZoneManager();
        PlayerStats stats = plugin.getStatsManager().getStats(player);
        PlayerState state = stateManager.getState(player);

        // First, quickly reduce any existing cold.
        state.reduceCold(NATURAL_REDUCTION_RATE);

        // Get zone-specific parameters with sane defaults.
        double heatIncrease = zoneManager.getZoneValue(player.getLocation(), "increase_per_second", 5.0);
        double maxHeat = zoneManager.getZoneValue(player.getLocation(), "max_value", 100.0);

        // Calculate the effective increase after resistance.
        double effectiveIncrease = heatIncrease * (1 - (stats.getHeatResistance() / 100.0));

        // Let the PlayerState handle its own modification.
        state.increaseHeat(effectiveIncrease, maxHeat);
    }

    /**
     * Handles the logic for a player inside a "cold" zone.
     */
    private void handleColdZone(Player player) {
        PlayerStateManager stateManager = plugin.getPlayerStateManager();
        ZoneManager zoneManager = plugin.getZoneManager();
        PlayerStats stats = plugin.getStatsManager().getStats(player);
        PlayerState state = stateManager.getState(player);

        state.reduceHeat(NATURAL_REDUCTION_RATE);

        double coldIncrease = zoneManager.getZoneValue(player.getLocation(), "increase_per_second", 5.0);
        double maxCold = zoneManager.getZoneValue(player.getLocation(), "max_value", 100.0);

        double effectiveIncrease = coldIncrease * (1 - (stats.getColdResistance() / 100.0));

        state.increaseCold(effectiveIncrease, maxCold);
    }

    /**
     * Handles the logic for a player in a neutral ("natural") zone.
     */
    private void handleNaturalZone(Player player) {
        PlayerState state = plugin.getPlayerStateManager().getState(player);

        // In a neutral zone, both heat and cold gradually decrease.
        state.reduceHeat(NATURAL_REDUCTION_RATE);
        state.reduceCold(NATURAL_REDUCTION_RATE);
    }

    /**
     * Applies gameplay effects (penalties) based on the player's current heat and cold levels.
     * @param player The player to apply penalties to.
     */
    private void applyPenalties(Player player) {
        PlayerState state = plugin.getPlayerStateManager().getState(player);

        // Example penalty for heat
        if (state.getHeat() >= 90) {
            player.setFireTicks(40); // Set on fire for 2 seconds
        }

        // Example penalty for cold
        if (state.getCold() > 0) {
            // Apply slowness or other effects here.
        }
    }
}