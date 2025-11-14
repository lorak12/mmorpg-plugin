package org.nakii.mmorpg.tasks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.RequirementManager;
import org.nakii.mmorpg.managers.WorldManager;
import org.nakii.mmorpg.world.CustomWorld;
import org.nakii.mmorpg.zone.Zone;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMovementTracker extends BukkitRunnable {

    private final WorldManager worldManager;
    private final RequirementManager requirementManager;
    private final ConcurrentHashMap<UUID, String> playerZoneCache = new ConcurrentHashMap<>();

    public PlayerMovementTracker(WorldManager worldManager, RequirementManager requirementManager) {
        this.worldManager = worldManager;
        this.requirementManager = requirementManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // --- NEW STATE-DRIVEN LOGIC ---
            // This now runs every time, ensuring the player is in the correct state.
            updatePlayerEffects(player);

            // --- Zone Change Logic (unchanged but now more reliable) ---
            Location playerLocation = player.getLocation();
            Zone currentZone = worldManager.getZoneForLocation(playerLocation);

            String lastKnownZoneId = playerZoneCache.get(player.getUniqueId());
            String currentZoneId = (currentZone != null) ? currentZone.getId() : null;

            if (Objects.equals(lastKnownZoneId, currentZoneId)) {
                continue;
            }

            if (currentZone != null) {
                handleZoneEntry(player, currentZone, playerLocation);
            }

            if (currentZoneId != null) {
                playerZoneCache.put(player.getUniqueId(), currentZoneId);
            } else {
                playerZoneCache.remove(player.getUniqueId());
            }
        }
    }

    /**
     * This method ensures a player has the correct Haste/Fatigue effects based on
     * their current world and game mode. It is self-correcting.
     */
    private void updatePlayerEffects(Player player) {
        CustomWorld customWorld = worldManager.getCustomWorld(player.getWorld().getName());

        boolean shouldHaveEffects = (customWorld != null && player.getGameMode() == GameMode.SURVIVAL);

        if (shouldHaveEffects) {
            // Player should have the effects. Apply them if they are missing.
            if (!player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 4, false, false, false));
            }
            if (!player.hasPotionEffect(PotionEffectType.HASTE)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, PotionEffect.INFINITE_DURATION, 2, false, false, false));
            }
        } else {
            // Player should NOT have the effects. Remove them if they exist.
            if (player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) {
                player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            }
            if (player.hasPotionEffect(PotionEffectType.HASTE)) {
                player.removePotionEffect(PotionEffectType.HASTE);
            }
        }
    }

    // handleZoneEntry and removePlayer methods are unchanged.
    private void handleZoneEntry(Player player, Zone toZone, Location fromLocation) {
        List<String> reqStrings = toZone.getFlags().entryRequirements();
        if (!requirementManager.meetsAll(player, reqStrings)) {
            player.teleport(fromLocation.clone().subtract(player.getVelocity()));
            player.sendMessage(Component.text("You do not meet the requirements to enter this area.", NamedTextColor.RED));
            return;
        }

        NamespacedKey zoneKey = new NamespacedKey(MMORPGCore.getInstance(), "visited_zone." + toZone.getId());
        if (!player.getPersistentDataContainer().has(zoneKey, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().set(zoneKey, PersistentDataType.BYTE, (byte) 1);
            Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(1000));
            Title title = Title.title(
                    Component.text("NEW AREA", NamedTextColor.YELLOW),
                    toZone.getDisplayName(),
                    times
            );
            player.showTitle(title);
        }
    }

    public void removePlayer(Player player) {
        playerZoneCache.remove(player.getUniqueId());
        // Also ensure effects are removed on quit
        if (player.hasPotionEffect(PotionEffectType.MINING_FATIGUE)) player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
        if (player.hasPotionEffect(PotionEffectType.HASTE)) player.removePotionEffect(PotionEffectType.HASTE);
    }
}