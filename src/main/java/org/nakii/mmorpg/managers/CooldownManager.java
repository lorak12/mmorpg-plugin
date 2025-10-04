package org.nakii.mmorpg.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    // Player UUID -> (Ability Key -> Cooldown Expiry Timestamp)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public void setCooldown(UUID playerId, String abilityKey, int seconds) {
        if (seconds <= 0) return;
        long expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(abilityKey.toUpperCase(), expiryTime);
    }

    public boolean isOnCooldown(UUID playerId, String abilityKey) {
        return getCooldownRemaining(playerId, abilityKey) > 0;
    }

    public long getCooldownRemaining(UUID playerId, String abilityKey) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null || !playerCooldowns.containsKey(abilityKey.toUpperCase())) {
            return 0;
        }

        long expiryTime = playerCooldowns.get(abilityKey.toUpperCase());
        long remaining = expiryTime - System.currentTimeMillis();

        if (remaining <= 0) {
            playerCooldowns.remove(abilityKey.toUpperCase());
            return 0;
        }

        return remaining;
    }
}