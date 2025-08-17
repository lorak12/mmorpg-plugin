package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;
import org.nakii.mmorpg.player.PlayerState;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerStateManager {

    private final ConcurrentHashMap<UUID, PlayerState> playerStates = new ConcurrentHashMap<>();

    public void addPlayer(Player player) {
        playerStates.put(player.getUniqueId(), new PlayerState());
    }

    public void removePlayer(Player player) {
        playerStates.remove(player.getUniqueId());
    }

    public PlayerState getState(Player player) {
        // This is a safeguard against race conditions or reloads.
        return playerStates.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerState());
    }
}