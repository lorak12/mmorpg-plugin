package org.nakii.mmorpg.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.events.PlayerBalanceChangeEvent;
import org.nakii.mmorpg.events.PluginTimeUpdateEvent;
import org.nakii.mmorpg.events.SlayerProgressUpdateEvent;
import org.nakii.mmorpg.managers.ScoreboardManager;

public class ScoreboardListener implements Listener {

    private final ScoreboardManager scoreboardManager;

    public ScoreboardListener(MMORPGCore plugin) {
        this.scoreboardManager = plugin.getScoreboardManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scoreboardManager.setScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        scoreboardManager.removeScoreboard(event.getPlayer());
    }

    // Listen for our custom event to update the time for all players
    @EventHandler
    public void onTimeUpdate(PluginTimeUpdateEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            scoreboardManager.updateScoreboard(player);
        }
    }

    @EventHandler
    public void onSlayerProgress(SlayerProgressUpdateEvent event) {
        scoreboardManager.updateScoreboard(event.getPlayer());
    }

    @EventHandler
    public void onBalanceChange(PlayerBalanceChangeEvent event) {
        scoreboardManager.updateScoreboard(event.getPlayer());
    }

    // In the future, you would add listeners for SlayerProgressUpdateEvent,
    // PlayerZoneChangeEvent, PlayerBalanceChangeEvent, etc. to trigger
    // more frequent and targeted updates.
}