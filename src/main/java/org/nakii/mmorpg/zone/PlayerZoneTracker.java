package org.nakii.mmorpg.zone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.events.PlayerZoneChangeEvent;
import org.nakii.mmorpg.managers.ZoneManager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A repeating task that tracks the current Zone of all online players.
 * It caches the last known zone and fires a PlayerZoneChangeEvent when a change is detected.
 */
public class PlayerZoneTracker extends BukkitRunnable {

    private final ZoneManager zoneManager;
    private final ConcurrentHashMap<UUID, Zone> playerZoneCache = new ConcurrentHashMap<>();

    public PlayerZoneTracker(MMORPGCore plugin) {
        this.zoneManager = plugin.getZoneManager();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location oldLocation = player.getLocation(); // Store location BEFORE the check
            Zone lastKnownZone = playerZoneCache.get(player.getUniqueId());
            Zone currentZone = zoneManager.getZoneForLocation(oldLocation);

            if (lastKnownZone != currentZone) {
                // Fire the event on the main server thread
                Bukkit.getScheduler().runTask(MMORPGCore.getInstance(), () -> {
                    // Pass the oldLocation into the event
                    PlayerZoneChangeEvent event = new PlayerZoneChangeEvent(player, oldLocation, lastKnownZone, currentZone);
                    Bukkit.getPluginManager().callEvent(event);
                });

                // Update the cache with the new, non-null zone, or remove if they entered wilderness
                if (currentZone != null) {
                    playerZoneCache.put(player.getUniqueId(), currentZone);
                } else {
                    playerZoneCache.remove(player.getUniqueId());
                }
            }
        }
    }

    /**
     * Removes a player from the tracking cache. Called on player quit to prevent memory leaks.
     * @param player The player to remove.
     */
    public void removePlayer(Player player) {
        playerZoneCache.remove(player.getUniqueId());
    }

    /**
     * Gets the cached zone for a player.
     * @param player The player.
     * @return The cached Zone, or null if not in a zone.
     */
    public Zone getCachedZone(Player player) {
        return playerZoneCache.get(player.getUniqueId());
    }
}