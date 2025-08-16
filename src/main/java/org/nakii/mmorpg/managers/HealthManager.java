package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerStats;

/**
 * Manages the passive health regeneration for all online players.
 */
public class HealthManager {

    private final MMORPGCore plugin;

    public HealthManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the repeating task that handles health regeneration for all players.
     * This should be called once in the plugin's onEnable method.
     */
    public void startHealthRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Iterate through every online player on the server
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isDead()) {
                        continue; // Skip dead players
                    }

                    // Get the player's maximum health directly from their entity attributes.
                    // This is the most reliable source, as it's set by our StatsManager.
                    double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                    double currentHealth = player.getHealth();

                    // Only regenerate health if the player is not at full health.
                    if (currentHealth < maxHealth) {
                        // Get the player's calculated stats, which includes their Health Regen value.
                        PlayerStats stats = plugin.getStatsManager().getStats(player);
                        double healthRegenStat = stats.getHealthRegen();

                        // --- Health Regeneration Formula ---
                        // Your documentation states that the base Health Regen is 100.
                        // A logical formula is that 100 regen = 1% of max health per second.
                        // We also add a small flat amount to ensure even low-level players regenerate.
                        double flatRegen = 0.5; // Regenerate at least 0.5 HP (1/4 of a heart) per second
                        double percentRegen = (healthRegenStat / 100.0) * (maxHealth / 100.0);
                        double totalRegenAmount = flatRegen + percentRegen;

                        // In the future, you can add a check here to pause regeneration during combat.
                        // For example: if (!plugin.getCombatTracker().isInCombat(player)) {
                        // Apply the regeneration, ensuring it doesn't exceed the player's max health.
                        player.setHealth(Math.min(maxHealth, currentHealth + totalRegenAmount));
                        // }
                    }
                }
            }
            // Run this task once every second (20 ticks).
        }.runTaskTimer(plugin, 20L, 20L);
    }
}