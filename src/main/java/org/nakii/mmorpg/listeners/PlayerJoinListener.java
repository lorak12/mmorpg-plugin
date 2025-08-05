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
        plugin.getSkillManager().loadPlayerData(player); // Load skill data on join
        plugin.getStatsManager().registerPlayer(player);
        plugin.getHealthManager().registerEntity(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getSkillManager().savePlayerData(player); // Save skill data on quit
        plugin.getStatsManager().unregisterPlayer(player);
        plugin.getHealthManager().unregisterEntity(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // A short delay to ensure the player's inventory is cleared before recalculating
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getStatsManager().recalculateStats(player);
            plugin.getHealthManager().updateMaxHealth(player);
            double maxHealth = plugin.getHealthManager().getMaxHealth(player);
            plugin.getHealthManager().setCurrentHealth(player, maxHealth);
        }, 1L);
    }
}