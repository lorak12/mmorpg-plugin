package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.slayer.PlayerSlayerData;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the cached slayer data for all online players.
 */
public class SlayerDataManager {

    private final ConcurrentHashMap<UUID, PlayerSlayerData> playerDataCache = new ConcurrentHashMap<>();

    public void addPlayer(Player player, PlayerSlayerData data) {
        playerDataCache.put(player.getUniqueId(), data);
    }

    public void removePlayer(Player player) {
        playerDataCache.remove(player.getUniqueId());
    }

    public PlayerSlayerData getData(Player player) {
        return playerDataCache.get(player.getUniqueId());
    }
}