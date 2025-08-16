package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;

public class PlayerConnectionListener implements Listener { // Or whatever you named it

    private final MMORPGCore plugin;

    public PlayerConnectionListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // --- THE FIX: Method was renamed from registerPlayer to loadPlayer ---
        plugin.getStatsManager().loadPlayer(player);
        plugin.getPlayerStateManager().loadPlayer(player);
        plugin.getEconomyManager().loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getStatsManager().unloadPlayer(player);
        plugin.getPlayerStateManager().unloadPlayer(player);
        plugin.getEconomyManager().unloadPlayer(event.getPlayer());
    }
}