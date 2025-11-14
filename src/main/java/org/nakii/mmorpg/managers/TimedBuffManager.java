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

    public record StatBuff(Stat stat, double amount, String source) {}

    private final Cache<String, StatBuff> activeBuffs;
    private final StatsManager statsManager;
    private final MMORPGCore plugin;

    public TimedBuffManager(MMORPGCore plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;

        RemovalListener<String, StatBuff> listener = notification -> {
            String[] parts = notification.getKey().split("-");
            UUID playerUUID = UUID.fromString(parts[0]);
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                // Schedule the stat recalculation on the main server thread.
                Bukkit.getScheduler().runTask(plugin, () -> {
                    this.statsManager.recalculateStats(player);
                });
            }
        };

        this.activeBuffs = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS) // Default duration
                .removalListener(listener)
                .build();
    }

    public void applyBuff(Player player, Stat stat, double amount, String source, int durationSeconds) {
        String key = player.getUniqueId() + "-" + source;
        StatBuff buff = new StatBuff(stat, amount, source);

        activeBuffs.put(key, buff);
        // Schedule a task to invalidate the specific key after the duration.
        Bukkit.getScheduler().runTaskLater(plugin, () -> activeBuffs.invalidate(key), durationSeconds * 20L);

        statsManager.recalculateStats(player);
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