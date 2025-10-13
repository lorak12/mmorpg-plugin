package org.nakii.mmorpg.quest.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * An interface abstracting the core functions of a hologram,
 * regardless of the underlying plugin provider.
 */
public interface IMmoHologram {

    /**
     * Makes the hologram visible to a specific player.
     */
    void show(Player player);

    /**
     * Hides the hologram from a specific player.
     */
    void hide(Player player);

    /**
     * Moves the hologram to a new location.
     */
    void move(Location location);

    /**
     * Permanently deletes the hologram.
     */
    void delete();

    /**
     * Checks if the hologram is currently visible to a specific player.
     */
    boolean isVisibleTo(Player player);

    /**
     * Checks if the underlying hologram object exists and is spawned.
     */
    boolean isSpawned();

    /**
     * Creates the physical hologram at a location with the given lines of text.
     */
    void spawn(Location location, List<String> lines);
}