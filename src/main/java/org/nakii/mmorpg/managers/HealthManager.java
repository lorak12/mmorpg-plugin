package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerStats;

public class HealthManager {

    private final MMORPGCore plugin;
    private final StatsManager statsManager;
    private final PlayerManager playerManager;
    private final CombatTracker combatTracker;

    public HealthManager(MMORPGCore plugin, StatsManager statsManager, PlayerManager playerManager, CombatTracker combatTracker) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.playerManager = playerManager;
        this.combatTracker = combatTracker;
    }

    /**
     * Starts the main health task. This task is now responsible for two things:
     * 1. Regenerating the player's MMORPG health when they are out of combat.
     * 2. Perfectly syncing the player's visual hearts to their MMORPG health percentage.
     */
    public void startHealthRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isDead()) continue;

                    PlayerStats stats = statsManager.getStats(player);
                    double maxMmorpgHealth = stats.getHealth();
                    double currentMmorpgHealth = playerManager.getCurrentHealth(player);

                    // --- 1. Combat-Aware Regeneration ---
                    // Regenerate the underlying MMORPG health value.
                    if (!combatTracker.isInCombat(player) && currentMmorpgHealth < maxMmorpgHealth) {
                        double healthRegenStat = stats.getHealthRegen();
                        double flatRegen = maxMmorpgHealth * 0.005; // Base regen is 0.5% of max health per second.
                        double percentRegen = (healthRegenStat / 100.0) * (maxMmorpgHealth / 100.0);
                        double totalRegenAmount = flatRegen + percentRegen;

                        playerManager.setCurrentHealth(player, currentMmorpgHealth + totalRegenAmount);
                        // We must re-fetch the health after regeneration for the visual sync.
                        currentMmorpgHealth = playerManager.getCurrentHealth(player);
                    }

                    // --- 2. PRECISE VISUAL HEALTH SCALING ---
                    // This is the new, correct logic for displaying hearts.

                    // Get the player's maximum visual hearts (should always be 40.0 now).
                    double maxVisualHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();

                    // Calculate the health percentage.
                    double healthPercent = currentMmorpgHealth / maxMmorpgHealth;

                    // Calculate the target visual health based on the percentage.
                    double targetVisualHealth = healthPercent * maxVisualHealth;

                    // Round the visual health to the nearest half-heart (0.5).
                    // Math.round(value * 2) / 2.0 is a standard trick for this.
                    double roundedVisualHealth = Math.round(targetVisualHealth * 2.0) / 2.0;

                    // If the calculation results in 0, but the player is still alive,
                    // force the display to show half a heart (1.0 health points).
                    if (roundedVisualHealth < 1.0 && currentMmorpgHealth > 0) {
                        roundedVisualHealth = 1.0;
                    }

                    // Set the player's actual, visible health to the final calculated value.
                    // This ensures it never exceeds the visual max.
                    player.setHealth(Math.min(roundedVisualHealth, maxVisualHealth));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run once per second
    }
}