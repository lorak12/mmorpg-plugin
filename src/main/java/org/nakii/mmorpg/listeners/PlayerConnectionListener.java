package org.nakii.mmorpg.listeners;

import net.citizensnpcs.api.event.CitizensEnableEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.collection.PlayerCollectionData;
import org.nakii.mmorpg.quest.NPCVisibilityManager;
import org.nakii.mmorpg.slayer.PlayerSlayerData;
import org.nakii.mmorpg.util.ChatUtils; // Assuming you have this for formatting

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
            plugin.getPlayerManager().loadPlayer(player);

            plugin.getQuestManager().loadPlayerData(player);

            // This is still important for players who join AFTER the server has started
            plugin.getNpcVisibilityManager().onPlayerJoin(event.getPlayer());

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
        // ... This method is unchanged and correct.
        Player player = event.getPlayer();

        try {
            plugin.getPlayerMovementTracker().removePlayer(event.getPlayer());
            plugin.getSkillManager().savePlayerData(player);
            plugin.getStatsManager().unloadPlayer(player);
            plugin.getEconomyManager().unloadPlayer(player);
            plugin.getPlayerManager().unloadPlayer(player);

            plugin.getHologramManager().onPlayerQuit(player);
            plugin.getQuestManager().unloadPlayerData(player);


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

        plugin.getSlayerDataManager().removePlayer(player);
        plugin.getCollectionManager().removePlayer(player);
        plugin.getScoreboardManager().removeScoreboard(player);
    }

    /**
     * This handler listens for the moment Citizens finishes loading all its NPCs at startup.
     * It then forces a visibility update for all players who might have logged in before
     * the NPCs were ready, fixing the startup visibility bug.
     */
    @EventHandler
    public void onCitizensEnabled(CitizensEnableEvent event) {
        plugin.getLogger().info("Citizens enabled. Applying initial NPC visibility for all online players...");
        NPCVisibilityManager visibilityManager = plugin.getNpcVisibilityManager();
        if (visibilityManager == null) return;

        // Iterate over any players already online and correct their NPC visibility state.
        for (Player player : Bukkit.getOnlinePlayers()) {
            visibilityManager.forceFullUpdateForPlayer(player);
        }
    }
}