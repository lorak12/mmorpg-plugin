package org.nakii.mmorpg.managers;

import org.bukkit.damage.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.stats.PlayerStats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnvironmentManager {

    private final MMORPGCore plugin;
    private final Map<UUID, Double> playerHeatMap = new HashMap<>();
    private final Map<UUID, Double> playerColdMap = new HashMap<>();

    public EnvironmentManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public double getPlayerHeat(Player player) {
        return playerHeatMap.getOrDefault(player.getUniqueId(), 0.0);
    }

    public double getPlayerCold(Player player) {
        return playerColdMap.getOrDefault(player.getUniqueId(), 0.0);
    }



    /**
     * Resets a player's environmental status, typically upon death/respawn.
     */
    public void resetPlayerEnvironment(Player player) {
        playerColdMap.put(player.getUniqueId(), 0.0);
        playerHeatMap.put(player.getUniqueId(), 0.0);
        player.removePotionEffect(PotionEffectType.SLOWNESS); // Also explicitly remove potion effects
    }

    public void startEnvironmentTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerEnvironment(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    private void updatePlayerEnvironment(Player player) {
        UUID uuid = player.getUniqueId();
        ZoneManager zoneManager = plugin.getZoneManager();
        String zoneType = zoneManager.getZoneEnvironmentType(player.getLocation());
        PlayerStats stats = plugin.getStatsManager().getStats(player);

        double currentHeat = getPlayerHeat(player);
        double currentCold = getPlayerCold(player);

        switch (zoneType) {
            case "hot":
                if (currentCold > 0) playerColdMap.put(uuid, Math.max(0, currentCold - 5));
                double heatIncrease = zoneManager.getZoneValue(player.getLocation(), "increase_per_second", 5.0);
                double effectiveHeatIncrease = heatIncrease * (1 - (stats.getHeatResistance() / 100.0));
                double maxHeat = zoneManager.getZoneValue(player.getLocation(), "max_value", 100.0);
                playerHeatMap.put(uuid, Math.min(maxHeat, currentHeat + effectiveHeatIncrease));
                break;

            case "cold":
                if (currentHeat > 0) playerHeatMap.put(uuid, Math.max(0, currentHeat - 5));
                double coldIncrease = zoneManager.getZoneValue(player.getLocation(), "increase_per_second", 5.0);
                double effectiveColdIncrease = coldIncrease * (1 - (stats.getColdResistance() / 100.0));
                double maxCold = zoneManager.getZoneValue(player.getLocation(), "max_value", 100.0);
                playerColdMap.put(uuid, Math.min(maxCold, currentCold + effectiveColdIncrease));
                break;

            default: // "natural" zone
                // Simply reduce the values. Do NOT try to remove effects here.
                if (currentHeat > 0) playerHeatMap.put(uuid, Math.max(0, currentHeat - 5));
                if (currentCold > 0) playerColdMap.put(uuid, Math.max(0, currentCold - 5));
                break;
        }

        // The penalty logic will handle applying AND removing effects based on the new values.
        applyPenalties(player);
    }

    private void applyPenalties(Player player) {
        // Heat Penalty
        double heat = getPlayerHeat(player);
        if (heat >= plugin.getZoneManager().getZoneValue(player.getLocation(), "damage_threshold", 90.0)) {
            double heatDamage = plugin.getZoneManager().getZoneValue(player.getLocation(), "damage_amount", 5.0);
            // This is a simple and reliable way to deal damage.
            // Our listener will catch this and see the cause is FIRE_TICK from the environment.
            player.setFireTicks(40); // Visually set them on fire briefly
        }

        // Slowness Penalty
        double cold = getPlayerCold(player);
        if (cold >= 25.0) {
            int slownessAmplifier = (int) (cold / 25.0) - 1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, slownessAmplifier, true, false, true));
        } else {
            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }

        // --- DEPRECATION FIX ---
        if (cold >= 100.0) {
            double coldDamage = plugin.getZoneManager().getZoneValue(player.getLocation(), "damage_at_max", 1.0);

            // The simplest, most robust, and non-deprecated way to apply environmental damage.
            // Our CombatListener will intercept this and see the cause is FREEZE.
            player.setFreezeTicks(player.getFreezeTicks() + 40);
        }
    }


}