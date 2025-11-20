package org.nakii.mmorpg.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CombatTracker {

    // This cache is for tracking hit combos on a specific target.
    public record HitData(UUID targetId, int hitCount) {}
    private final Cache<UUID, HitData> hitComboCache;

    // --- NEW: A simple cache to track a player's "in combat" state ---
    // Any player UUID in this cache is considered in combat.
    // The entry automatically expires after 15 seconds of inactivity.
    private final Cache<UUID, Boolean> combatTagCache;

    public CombatTracker() {
        this.hitComboCache = CacheBuilder.newBuilder()
                .expireAfterWrite(4, TimeUnit.SECONDS) // Combo expires after 4 seconds
                .build();

        this.combatTagCache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.SECONDS) // Combat tag expires after 2 seconds
                .build();
    }

    // --- Methods for Hit Combos (unchanged) ---
    public HitData getHitData(Player player) {
        return hitComboCache.getIfPresent(player.getUniqueId());
    }

    public void recordHit(Player player, Entity target) {
        HitData lastHit = getHitData(player);
        int newHitCount = (lastHit != null && lastHit.targetId().equals(target.getUniqueId())) ? lastHit.hitCount() + 1 : 1;
        hitComboCache.put(player.getUniqueId(), new HitData(target.getUniqueId(), newHitCount));

        // Also, tag the player as in-combat.
        tag(player);
        if(target instanceof Player victim) {
            tag(victim);
        }
    }

    // --- NEW: Methods for Combat State ---

    /**
     * Marks a player as being in combat for 15 seconds.
     * @param player The player to tag.
     */
    public void tag(Player player) {
        combatTagCache.put(player.getUniqueId(), true);
    }

    /**
     * Checks if a player is currently tagged as in-combat.
     * @param player The player to check.
     * @return true if the player has dealt or received damage in the last 2 seconds.
     */
    public boolean isInCombat(Player player) {
        return combatTagCache.getIfPresent(player.getUniqueId()) != null;
    }
}