package org.nakii.mmorpg.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.nakii.mmorpg.player.PlayerState;

public class PlayerStateManager {
    private final Map<UUID, PlayerState> playerStates = new HashMap<>();

    public void loadPlayer(Player player) {
        playerStates.put(player.getUniqueId(), new PlayerState());
    }

    public void unloadPlayer(Player player) {
        playerStates.remove(player.getUniqueId());
    }

    public PlayerState getState(Player player) {
        return playerStates.getOrDefault(player.getUniqueId(), new PlayerState());
    }
}