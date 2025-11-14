package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.collection.PlayerCollectionData;
import org.nakii.mmorpg.managers.*;
import org.nakii.mmorpg.slayer.PlayerSlayerData;
import org.nakii.mmorpg.tasks.PlayerMovementTracker;
import org.nakii.mmorpg.util.ChatUtils;

import java.sql.SQLException;

public class PlayerConnectionListener implements Listener {

    private final MMORPGCore plugin;
    private final DatabaseManager databaseManager;
    private final SkillManager skillManager;
    private final SlayerDataManager slayerDataManager;
    private final CollectionManager collectionManager;
    private final StatsManager statsManager;
    private final PlayerManager playerManager;
    private final EconomyManager economyManager;
    private final SlayerManager slayerManager;
    private final ScoreboardManager scoreboardManager;
    private final PlayerMovementTracker playerMovementTracker;

    public PlayerConnectionListener(MMORPGCore plugin, DatabaseManager databaseManager, SkillManager skillManager,
                                    SlayerDataManager slayerDataManager, CollectionManager collectionManager,
                                    StatsManager statsManager, PlayerManager playerManager, EconomyManager economyManager,
                                    SlayerManager slayerManager, ScoreboardManager scoreboardManager,
                                    PlayerMovementTracker playerMovementTracker) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.skillManager = skillManager;
        this.slayerDataManager = slayerDataManager;
        this.collectionManager = collectionManager;
        this.statsManager = statsManager;
        this.playerManager = playerManager;
        this.economyManager = economyManager;
        this.slayerManager = slayerManager;
        this.scoreboardManager = scoreboardManager;
        this.playerMovementTracker = playerMovementTracker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            skillManager.loadPlayerData(player);
            slayerDataManager.addPlayer(player, databaseManager.loadPlayerSlayerData(player));
            collectionManager.addPlayer(player, databaseManager.loadPlayerCollectionData(player));
            statsManager.loadPlayerData(player); // Loads bonus stats

            // Recalculate stats now that dependencies are loaded
            statsManager.recalculateStats(player);

            playerManager.loadPlayer(player);
            economyManager.loadPlayer(player);
            slayerManager.loadQuestForPlayer(player);
            scoreboardManager.setScoreboard(player);
        } catch (SQLException e) {
            e.printStackTrace();
            player.kick(ChatUtils.format("<red>Error: Could not load your player data. Please contact an administrator."));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        try {
            playerMovementTracker.removePlayer(player);
            skillManager.savePlayerData(player);
            statsManager.unloadPlayerData(player); // Saves bonus stats
            economyManager.unloadPlayer(player);
            playerManager.unloadPlayer(player);

            PlayerSlayerData slayerData = slayerDataManager.getData(player);
            if (slayerData != null) {
                databaseManager.savePlayerSlayerData(player, slayerData);
            }

            PlayerCollectionData collectionData = collectionManager.getData(player);
            if (collectionData != null) {
                databaseManager.savePlayerCollectionData(player, collectionData);
            }

            slayerManager.saveQuestForPlayer(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        slayerDataManager.removePlayer(player);
        collectionManager.removePlayer(player);
        scoreboardManager.removeScoreboard(player);
        statsManager.unloadPlayer(player); // Unloads main stats cache
    }
}