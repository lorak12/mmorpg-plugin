package org.nakii.mmorpg.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CombatTracker {

    public record HitData(UUID targetId, int hitCount) {}

    private final Cache<UUID, HitData> hitComboCache;

    public CombatTracker() {
        this.hitComboCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.SECONDS)
                .build();
    }

    public HitData getHitData(Player player) {
        return hitComboCache.getIfPresent(player.getUniqueId());
    }

    public void recordHit(Player player, Entity target) {
        HitData lastHit = getHitData(player);
        int newHitCount = 1;

        if (lastHit != null && lastHit.targetId().equals(target.getUniqueId())) {
            newHitCount = lastHit.hitCount() + 1;
        }

        hitComboCache.put(player.getUniqueId(), new HitData(target.getUniqueId(), newHitCount));
    }
}