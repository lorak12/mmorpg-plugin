package org.nakii.mmorpg.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.nakii.mmorpg.zone.Zone;

import javax.annotation.Nullable;

/**
 * Called when a player moves from one Zone to another, or into/out of a zoned area.
 */
public class PlayerZoneChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Location fromLocation;
    private final Zone fromZone;
    private final Zone toZone;

    /**
     * @param player The player who changed zones.
     * @param fromZone The Zone the player is leaving. Can be null if they were in an un-zoned area.
     * @param toZone The Zone the player is entering. Can be null if they are entering an un-zoned area.
     */
    public PlayerZoneChangeEvent(Player player, Location fromLocation, @Nullable Zone fromZone, @Nullable Zone toZone) {
        this.player = player;
        this.fromLocation = fromLocation;
        this.fromZone = fromZone;
        this.toZone = toZone;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the zone the player is leaving.
     * @return The previous zone, or null if the player was not in a defined zone.
     */
    @Nullable
    public Zone getFromZone() {
        return fromZone;
    }

    /**
     * Gets the zone the player is entering.
     * @return The new zone, or null if the player is now in an un-defined zone.
     */
    @Nullable
    public Zone getToZone() {
        return toZone;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Location getFromLocation() {
        return fromLocation;
    }
}