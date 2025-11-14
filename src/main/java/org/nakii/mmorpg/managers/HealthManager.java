package org.nakii.mmorpg.managers;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.player.PlayerStats;

public class HealthManager {

    private final MMORPGCore plugin;
    private final StatsManager statsManager;

    public HealthManager(MMORPGCore plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    public void startHealthRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isDead()) {
                        continue;
                    }

                    double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                    double currentHealth = player.getHealth();

                    if (currentHealth < maxHealth) {
                        PlayerStats stats = statsManager.getStats(player);
                        double healthRegenStat = stats.getHealthRegen();
                        double flatRegen = 0.5;
                        double percentRegen = (healthRegenStat / 100.0) * (maxHealth / 100.0);
                        double totalRegenAmount = flatRegen + percentRegen;

                        player.setHealth(Math.min(maxHealth, currentHealth + totalRegenAmount));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}