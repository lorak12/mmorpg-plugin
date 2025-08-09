package org.nakii.mmorpg.listeners;

import org.nakii.mmorpg.MMORPGCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerJoinListener implements Listener {

    private final MMORPGCore plugin;

    public PlayerJoinListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. This call is essential as it triggers the initial recalculateStats()
        plugin.getStatsManager().registerPlayer(player);

        // 2. Load other persistent data like skill experience
        plugin.getSkillManager().loadPlayerData(player);

        // 3. --- THIS IS THE FIX ---
        // After stats have been fully calculated, get the player's max health from the StatsManager...
        double maxHealth = plugin.getStatsManager().getStats(player).getHealth();

        // ...and use the correct method to set their starting health in the HealthManager.
        // This ensures they log in with full custom health.
        plugin.getHealthManager().setEntityHealth(player, maxHealth);
        // --- END OF FIX ---
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getSkillManager().savePlayerData(player); // Save skill data on quit
        plugin.getStatsManager().unregisterPlayer(player);
        plugin.getHealthManager().unregisterEntity(player);
    }

    /**
     * Handles restoring a player's custom stats and health after they respawn.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // A 1-tick delay is best practice to ensure the player has fully respawned
        // before we start modifying their stats and health.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

            // 1. First, recalculate stats. This is crucial as their inventory might
            //    have changed, which affects their max health stat. This also updates
            //    their vanilla max health attribute.
            plugin.getStatsManager().recalculateStats(player);

            // 2. --- THIS IS THE FIX ---
            // After stats have been fully recalculated, get the player's new max health...
            double maxHealth = plugin.getStatsManager().getStats(player).getHealth();

            // ...and use the correct method to set their custom health back to full.
            plugin.getHealthManager().setEntityHealth(player, maxHealth);
            // --- END OF FIX ---
            plugin.getEnvironmentManager().resetPlayerEnvironment(player);

        }, 1L);
    }
}