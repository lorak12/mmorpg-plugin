package org.nakii.mmorpg.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CombatTracker {

    // A record to store combo data.
    public record HitData(UUID targetId, int hitCount) {}

    // The cache now stores the HitData object for each player.
    // Expires 15 seconds after the last hit, resetting the combo.
    private final Cache<UUID, HitData> hitComboCache;

    public CombatTracker() {
        this.hitComboCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Gets the current hit data for a player.
     * @return The HitData object, or null if no combo is active.
     */
    public HitData getHitData(Player player) {
        return hitComboCache.getIfPresent(player.getUniqueId());
    }

    /**
     * Records a hit, updating the combo counter.
     * If the target is different from the last one, the combo resets to 1.
     */
    public void recordHit(Player player, Entity target) {
        HitData lastHit = getHitData(player);
        int newHitCount = 1;

        if (lastHit != null && lastHit.targetId().equals(target.getUniqueId())) {
            // Same target, increment the combo
            newHitCount = lastHit.hitCount() + 1;
        }
        // If it's a new target, newHitCount remains 1, resetting the combo.

        hitComboCache.put(player.getUniqueId(), new HitData(target.getUniqueId(), newHitCount));
    }
}