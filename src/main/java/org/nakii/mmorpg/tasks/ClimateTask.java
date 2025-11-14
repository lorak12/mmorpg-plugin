package org.nakii.mmorpg.tasks;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.PlayerStateManager;
import org.nakii.mmorpg.managers.StatsManager;
import org.nakii.mmorpg.managers.WorldManager;
import org.nakii.mmorpg.player.PlayerState;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.util.ChatUtils;
import org.nakii.mmorpg.zone.Climate;
import org.nakii.mmorpg.zone.Zone;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClimateTask extends BukkitRunnable {

    private final MMORPGCore plugin;
    private final WorldManager worldManager;
    private final PlayerStateManager stateManager;
    private final StatsManager statsManager;
    private final Set<UUID> debuggingPlayers = Collections.synchronizedSet(new HashSet<>());

    // --- CONFIGURABLE VALUES ---
    private final double heatPerTick;
    private final double coldPerTick;
    private final double naturalDecrease;
    private final int damageThreshold;
    private final double damageScaling;
    private final int freezeTicks;
    private final int slownessDuration;
    private final int slownessAmplifier;
    private final int fireTicks;
    private final int weaknessDuration;
    private final int weaknessAmplifier;

    public ClimateTask(MMORPGCore plugin, WorldManager worldManager, PlayerStateManager stateManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.stateManager = stateManager;
        this.statsManager = statsManager;

        // Load values from config
        FileConfiguration config = plugin.getConfig();
        this.heatPerTick = config.getDouble("climate.heat-per-tick", 1.0);
        this.coldPerTick = config.getDouble("climate.cold-per-tick", 1.0);
        this.naturalDecrease = config.getDouble("climate.natural-decrease", 1.5);
        this.damageThreshold = config.getInt("climate.damage-threshold", 10);
        this.damageScaling = config.getDouble("climate.damage-scaling", 3.5);
        this.freezeTicks = config.getInt("climate.freeze-ticks", 60);
        this.slownessDuration = config.getInt("climate.slowness-duration", 60);
        this.slownessAmplifier = config.getInt("climate.slowness-amplifier", 2);
        this.fireTicks = config.getInt("climate.fire-ticks", 60);
        this.weaknessDuration = config.getInt("climate.weakness-duration", 60);
        this.weaknessAmplifier = config.getInt("climate.weakness-amplifier", 1);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Zone zone = worldManager.getZoneForLocation(player.getLocation());
            PlayerState state = stateManager.getState(player);
            Climate climate = (zone != null && zone.getFlags().climate() != null) ? zone.getFlags().climate() : null;

            if (climate == null || "NEUTRAL".equalsIgnoreCase(climate.type())) {
                state.addHeat(-naturalDecrease);
                state.addCold(-naturalDecrease);
            } else if ("HOT".equalsIgnoreCase(climate.type())) {
                state.addCold(-naturalDecrease);
                double heatResistance = statsManager.getStats(player).getStat(Stat.HEAT_RESISTANCE);
                if (heatResistance < climate.requiredResistance()) {
                    state.addHeat(heatPerTick);
                    if (state.getHeatLevel() >= damageThreshold) {
                        applyHotEffects(player, state);
                    }
                } else {
                    state.addHeat(-naturalDecrease);
                }
            } else if ("COLD".equalsIgnoreCase(climate.type())) {
                state.addHeat(-naturalDecrease);
                double coldResistance = statsManager.getStats(player).getStat(Stat.COLD_RESISTANCE);
                if (coldResistance < climate.requiredResistance()) {
                    state.addCold(coldPerTick);
                    if (state.getColdLevel() >= damageThreshold) {
                        applyColdEffects(player, state);
                    }
                } else {
                    state.addCold(-naturalDecrease);
                }
            }

            if (debuggingPlayers.contains(player.getUniqueId())) {
                sendDebugInfo(player, state);
            }
        }
    }

    private void applyHotEffects(Player player, PlayerState state) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
                double rawDamage = Math.pow(state.getHeatLevel(), 2) * damageScaling;
                player.damage(Math.round(rawDamage));
                player.setFireTicks(fireTicks);
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, weaknessDuration, weaknessAmplifier));
            }
        }.runTask(plugin);
    }

    private void applyColdEffects(Player player, PlayerState state) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
                double rawDamage = Math.pow(state.getColdLevel(), 2) * damageScaling;
                player.damage(Math.round(rawDamage));
                player.setFreezeTicks(freezeTicks);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessDuration, slownessAmplifier));
            }
        }.runTask(plugin);
    }

    private void sendDebugInfo(Player player, PlayerState state) {
        PlayerStats stats = statsManager.getStats(player);
        double coldResistance = stats.getStat(Stat.COLD_RESISTANCE);
        double heatResistance = stats.getStat(Stat.HEAT_RESISTANCE);

        String message = String.format("<blue>Cold: <white>%.1f</white> | <red>Heat: <white>%.1f</white> || <aqua>Cold Res: <white>%.0f</white> | <gold>Heat Res: <white>%.0f</gold>",
                state.getColdLevel(),
                state.getHeatLevel(),
                coldResistance,
                heatResistance);

        player.sendMessage(ChatUtils.format(message));
    }

    public boolean toggleDebug(Player player) {
        UUID uuid = player.getUniqueId();
        if (debuggingPlayers.contains(uuid)) {
            debuggingPlayers.remove(uuid);
            return false;
        } else {
            debuggingPlayers.add(uuid);
            return true;
        }
    }

    public void removeDebugger(Player player) {
        debuggingPlayers.remove(player.getUniqueId());
    }
}