package org.nakii.mmorpg.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.slayer.PlayerSlayerData;

import java.sql.SQLException;

public class PlayerConnectionListener implements Listener { // Or whatever you named it

    private final MMORPGCore plugin;

    public PlayerConnectionListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
        Player player = event.getPlayer();
        // --- THE FIX: Method was renamed from registerPlayer to loadPlayer ---
        plugin.getStatsManager().loadPlayer(player);
        plugin.getEconomyManager().loadPlayer(event.getPlayer());

        // Slayer data loading
        PlayerSlayerData slayerData = plugin.getDatabaseManager().loadPlayerSlayerData(player);
        plugin.getSlayerDataManager().addPlayer(player, slayerData);

        try {
            plugin.getSlayerManager().loadQuestForPlayer(player);
        } catch (SQLException e) {
            e.printStackTrace();
            player.kick(Component.text("Failed to load your player data. Please contact an admin."));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) throws SQLException {
        Player player = event.getPlayer();
        plugin.getStatsManager().unloadPlayer(player);
        plugin.getEconomyManager().unloadPlayer(event.getPlayer());


        // Slayer data loading
        PlayerSlayerData slayerData = plugin.getSlayerDataManager().getData(player);
        if (slayerData != null) {
            plugin.getDatabaseManager().savePlayerSlayerData(player, slayerData);
        }
        try {
            plugin.getSlayerManager().saveQuestForPlayer(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        plugin.getSlayerDataManager().removePlayer(player);
    }
}