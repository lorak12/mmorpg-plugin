package org.nakii.mmorpg.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.collection.PlayerCollectionData;
import org.nakii.mmorpg.slayer.PlayerSlayerData;
import org.nakii.mmorpg.utils.ChatUtils; // Assuming you have this for formatting

import java.sql.SQLException;

public class PlayerConnectionListener implements Listener {

    private final MMORPGCore plugin;

    public PlayerConnectionListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {
            // --- CRITICAL LOADING ORDER ---
            // Load all data that StatsManager depends on FIRST.
            plugin.getSkillManager().loadPlayerData(player);
            plugin.getSlayerDataManager().addPlayer(player, plugin.getDatabaseManager().loadPlayerSlayerData(player));
            plugin.getCollectionManager().addPlayer(player, plugin.getDatabaseManager().loadPlayerCollectionData(player));

            // Now that Skill data is in the cache, it is safe to load StatsManager, which
            // triggers the first recalculateStats.
            plugin.getStatsManager().loadPlayer(player);

            // Load remaining data
            plugin.getEconomyManager().loadPlayer(player);
            plugin.getSlayerManager().loadQuestForPlayer(player);

            // Set up scoreboard last, after all data is loaded
            plugin.getScoreboardManager().setScoreboard(player);

        } catch (SQLException e) {
            e.printStackTrace();
            player.kick(ChatUtils.format("<red>Error: Could not load your player data. Please contact an administrator."));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        try {
            // Save all data before unloading/removing from caches
            plugin.getSkillManager().savePlayerData(player);
            plugin.getStatsManager().unloadPlayer(player);
            plugin.getEconomyManager().unloadPlayer(player);

            PlayerSlayerData slayerData = plugin.getSlayerDataManager().getData(player);
            if (slayerData != null) {
                plugin.getDatabaseManager().savePlayerSlayerData(player, slayerData);
            }

            PlayerCollectionData collectionData = plugin.getCollectionManager().getData(player);
            if (collectionData != null) {
                plugin.getDatabaseManager().savePlayerCollectionData(player, collectionData);
            }

            plugin.getSlayerManager().saveQuestForPlayer(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Unload/remove from all caches
        plugin.getSlayerDataManager().removePlayer(player);
        plugin.getCollectionManager().removePlayer(player);
        plugin.getScoreboardManager().removeScoreboard(player);
    }
}