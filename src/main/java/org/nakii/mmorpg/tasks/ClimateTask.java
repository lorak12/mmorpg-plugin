package org.nakii.mmorpg.tasks;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.nakii.mmorpg.utils.ChatUtils;
import org.nakii.mmorpg.zone.Climate;
import org.nakii.mmorpg.zone.Zone;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClimateTask extends BukkitRunnable {

    private final WorldManager worldManager;
    private final PlayerStateManager stateManager;
    private final StatsManager statsManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Set<UUID> debuggingPlayers = Collections.synchronizedSet(new HashSet<>());

    // ===== CONFIGURABLE CONSTANTS =====
    private static final double HEAT_PER_TICK = 1.0;
    private static final double COLD_PER_TICK = 1.0;
    private static final double NATURAL_DECREASE = 1.5;
    private static final int DAMAGE_THRESHOLD = 10;          // From which cold level dmg starts
    private static final double DAMAGE_SCALING = 3.5;       // Base scaling factor for dmg
    private static final int FREEZE_TICKS = 60;             // Duration of freezing animation
    private static final int SLOWNESS_DURATION = 60;        // Slowness duration in ticks
    private static final int SLOWNESS_AMPLIFIER = 2;        // Slowness level (0 = lvl1)
    private static final int FIRE_TICKS = 60;               // Fire ticks (burning effect)
    private static final int WEAKNESS_DURATION = 60;        // Weakness duration in ticks
    private static final int WEAKNESS_AMPLIFIER = 1;        // Weakness level (0 = lvl1)
    // ==================================


    public ClimateTask(MMORPGCore plugin) {
        this.worldManager = plugin.getWorldManager();
        this.stateManager = plugin.getPlayerStateManager();
        this.statsManager = plugin.getStatsManager();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // --- CORE CHANGE: Get the zone directly from the WorldManager ---
            Zone zone = worldManager.getZoneForLocation(player.getLocation());
            PlayerState state = stateManager.getState(player);
            Climate climate = (zone != null && zone.getFlags().climate() != null)
                    ? zone.getFlags().climate()
                    : null;

            if (climate == null || "NEUTRAL".equalsIgnoreCase(climate.type())) {
                // Player is in a neutral zone, decrease both heat and cold levels.
                state.addHeat(-NATURAL_DECREASE);
                state.addCold(-NATURAL_DECREASE);

            } else if ("HOT".equalsIgnoreCase(climate.type())) {
                // Decrease any residual cold.
                state.addCold(-NATURAL_DECREASE);

                double heatResistance = statsManager.getStats(player).getStat(Stat.HEAT_RESISTANCE);
                if (heatResistance < climate.requiredResistance()) {
                    state.addHeat(HEAT_PER_TICK);
                    // Set player on fire for visual effect, damage is vanilla.
                    if (state.getHeatLevel() >= DAMAGE_THRESHOLD) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!player.isOnline()) return;
                                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

                                // exponential damage scaling
                                double rawDamage = Math.pow(state.getHeatLevel(), 2) * DAMAGE_SCALING;
                                int calculatedDamage = (int) Math.round(rawDamage);

                                player.damage(calculatedDamage);
                                player.setFireTicks(FIRE_TICKS);
                                player.addPotionEffect(new PotionEffect(
                                        PotionEffectType.WEAKNESS,
                                        WEAKNESS_DURATION,
                                        WEAKNESS_AMPLIFIER
                                ));
                            }
                        }.runTask(MMORPGCore.getInstance());
                    }
                } else {
                    // Player has enough resistance, they cool down.
                    state.addHeat(-NATURAL_DECREASE);
                }

            } else if ("COLD".equalsIgnoreCase(climate.type())) {
                // Decrease any residual heat.
                state.addHeat(-NATURAL_DECREASE);

                double coldResistance = statsManager.getStats(player).getStat(Stat.COLD_RESISTANCE);
                if (coldResistance < climate.requiredResistance()) {
                    state.addCold(COLD_PER_TICK);
                    // If cold level is over the threshold, apply scaling damage.
                    if (state.getColdLevel() >= DAMAGE_THRESHOLD) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!player.isOnline()) return;
                                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

                                // exponential damage scaling
                                double rawDamage = Math.pow(state.getColdLevel(), 2) * DAMAGE_SCALING;
                                int calculatedDamage = (int) Math.round(rawDamage);

                                player.damage(calculatedDamage);
                                player.setFreezeTicks(FREEZE_TICKS);
                                player.addPotionEffect(new PotionEffect(
                                        PotionEffectType.SLOWNESS,
                                        SLOWNESS_DURATION,
                                        SLOWNESS_AMPLIFIER
                                ));
                            }
                        }.runTask(MMORPGCore.getInstance());
                    }
                } else {
                    // Player has enough resistance, they warm up.
                    state.addCold(-NATURAL_DECREASE);
                }
            }

            // If the player is debugging, send them the action bar update.
            if (debuggingPlayers.contains(player.getUniqueId())) {
                sendDebugInfo(player, state);
            }
        }
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