package org.nakii.mmorpg.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DebuffManager {

    // A simple record to store the stack count and the plugin that applied it (for future use).
    public record Debuff(int stacks, String source) {}

    private final Cache<UUID, Debuff> lethalityCache;
    private final Cache<UUID, Debuff> venomousCache;

    public DebuffManager() {
        // Cache for Lethality, expires 4 seconds after the last hit.
        this.lethalityCache = CacheBuilder.newBuilder()
                .expireAfterWrite(4, TimeUnit.SECONDS)
                .build();

        // Cache for Venomous, expires 5 seconds after the last hit.
        this.venomousCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .build();
    }

    // --- Methods for Lethality ---
    public int getLethalityStacks(UUID entityId) {
        Debuff debuff = lethalityCache.getIfPresent(entityId);
        return debuff != null ? debuff.stacks() : 0;
    }

    public void applyLethalityStack(UUID entityId, int newStackCount) {
        lethalityCache.put(entityId, new Debuff(newStackCount, "lethality"));
    }

    // --- Methods for Venomous ---
    public int getVenomousStacks(UUID entityId) {
        Debuff debuff = venomousCache.getIfPresent(entityId);
        return debuff != null ? debuff.stacks() : 0;
    }

    public void applyVenomousStack(UUID entityId, int newStackCount) {
        venomousCache.put(entityId, new Debuff(newStackCount, "venomous"));
    }
}