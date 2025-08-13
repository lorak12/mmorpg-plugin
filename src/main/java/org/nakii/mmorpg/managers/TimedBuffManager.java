package org.nakii.mmorpg.managers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.Stat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TimedBuffManager {

    // A record to hold the details of a single stat buff
    public record StatBuff(Stat stat, double amount, String source) {}

    // A cache where the Key is a unique identifier for the buff (e.g., "playerUUID-counter_strike")
    // and the value is the StatBuff object.
    private final Cache<String, StatBuff> activeBuffs;

    public TimedBuffManager() {
        // When a buff is removed from the cache (because its time expired),
        // we need to tell the StatsManager to recalculate stats for that player.
        RemovalListener<String, StatBuff> listener = notification -> {
            String[] parts = notification.getKey().split("-");
            UUID playerUUID = UUID.fromString(parts[0]);
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                // Schedule the stat recalc for the main thread
                Bukkit.getScheduler().runTask(MMORPGCore.getInstance(), () -> {
                    MMORPGCore.getInstance().getStatsManager().recalculateStats(player);
                });
            }
        };

        this.activeBuffs = CacheBuilder.newBuilder()
                .expireAfterWrite(7, TimeUnit.SECONDS) // Default duration
                .removalListener(listener)
                .build();
    }

    public void applyBuff(Player player, Stat stat, double amount, String source, int durationSeconds) {
        String key = player.getUniqueId().toString() + "-" + source;
        StatBuff buff = new StatBuff(stat, amount, source);

        // In a real system, you'd handle stacking logic here. For now, we just overwrite.
        activeBuffs.put(key, buff);

        // Immediately recalculate stats to apply the buff
        MMORPGCore.getInstance().getStatsManager().recalculateStats(player);
    }

    public double getBuffsForStat(Player player, Stat stat) {
        double total = 0;
        for (Map.Entry<String, StatBuff> entry : activeBuffs.asMap().entrySet()) {
            if (entry.getKey().startsWith(player.getUniqueId().toString()) && entry.getValue().stat() == stat) {
                total += entry.getValue().amount();
            }
        }
        return total;
    }
}