package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.economy.PlayerEconomy;
import org.nakii.mmorpg.events.PlayerBalanceChangeEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager implements Listener { // <-- Implement Listener

    private final MMORPGCore plugin;
    private final Map<UUID, PlayerEconomy> economyData = new HashMap<>();

    public EconomyManager(MMORPGCore plugin) {
        this.plugin = plugin;
        // Register this manager itself as a listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * --- THIS IS THE NEW EVENT HANDLER ---
     * Listens for any change to a player's balance and saves their data immediately.
     * This is the single source of truth for saving economy data.
     */
    @EventHandler
    public void onBalanceChange(PlayerBalanceChangeEvent event) {
        Player player = event.getPlayer();
        if (economyData.containsKey(player.getUniqueId())) {
            try {
                plugin.getDatabaseManager().savePlayerEconomy(player.getUniqueId(), getEconomy(player));
            } catch (SQLException e) {
                plugin.getLogger().severe("CRITICAL: Failed to save economy data for " + player.getName() + " after balance change!");
                e.printStackTrace();
            }
        }
    }

    public void loadPlayer(Player player) {
        try {
            PlayerEconomy data = plugin.getDatabaseManager().loadPlayerEconomy(player.getUniqueId());
            economyData.put(player.getUniqueId(), data);
        } catch (SQLException e) {
            e.printStackTrace();
            economyData.put(player.getUniqueId(), new PlayerEconomy(player.getUniqueId()));
        }
    }

    public void unloadPlayer(Player player) {
        // We can optionally save one last time here, but the event handler
        // should have already saved the most recent change.
        if (economyData.containsKey(player.getUniqueId())) {
            try {
                plugin.getDatabaseManager().savePlayerEconomy(player.getUniqueId(), getEconomy(player));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        economyData.remove(player.getUniqueId());
    }

    public PlayerEconomy getEconomy(Player player) {
        return economyData.getOrDefault(player.getUniqueId(), new PlayerEconomy(player.getUniqueId()));
    }

}