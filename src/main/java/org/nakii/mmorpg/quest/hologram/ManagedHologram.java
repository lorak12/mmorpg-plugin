package org.nakii.mmorpg.quest.hologram;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.quest.model.QuestHologram;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the state and lifecycle of a single quest hologram.
 * It holds the hologram implementation and its configuration.
 */
public class ManagedHologram {

    private final IMmoHologram hologram;
    private final QuestHologram template;
    private final Set<UUID> visibleTo = new HashSet<>();

    public ManagedHologram(QuestHologram template) {
        this.template = template;
        this.hologram = new DecentMmoHologram(); // Use our DecentHolograms implementation

        // Initial attempt to spawn. It's okay if this fails due to load order.
        trySpawn();
    }

    public String getId() {
        return template.getId();
    }

    public QuestHologram getTemplate() {
        return template;
    }

    /**
     * The core logic for showing or hiding the hologram for a player based on conditions.
     */
    public void updateVisibilityFor(Player player, boolean shouldBeVisible) {
        // --- NEW LOGIC ---
        // Lazily attempt to spawn the hologram if it doesn't exist yet.
        trySpawn();
        if (!hologram.isSpawned()) return;

        boolean isCurrentlyVisible = visibleTo.contains(player.getUniqueId());

        if (shouldBeVisible && !isCurrentlyVisible) {
            hologram.show(player);
            visibleTo.add(player.getUniqueId());
        } else if (!shouldBeVisible && isCurrentlyVisible) {
            hologram.hide(player);
            visibleTo.remove(player.getUniqueId());
        }
    }

    /**
     * If the hologram is attached to an NPC, this updates its position.
     */
    public void updatePosition() {
        // --- NEW LOGIC ---
        // Also lazily attempts to spawn the hologram.
        trySpawn();
        if (!hologram.isSpawned() || !template.isAttachedToNpc()) return;

        NPC npc = CitizensAPI.getNPCRegistry().getById(template.getNpcId());
        if (npc != null && npc.isSpawned()) {
            Location npcLocation = npc.getStoredLocation().clone().add(0, template.getOffset(), 0);
            hologram.move(npcLocation);
        }
    }

    /**
     * Cleans up the player's visibility state when they log out.
     */
    public void removePlayer(Player player) {
        visibleTo.remove(player.getUniqueId());
    }

    /**
     * Permanently destroys the hologram.
     */
    public void destroy() {
        hologram.delete();
    }

    /**
     * A new private method to handle spawning, preventing code duplication.
     */
    private void trySpawn() {
        if (hologram.isSpawned()) {
            return; // Already exists, do nothing.
        }

        Location spawnLocation = getInitialLocation();
        if (spawnLocation != null) {
            hologram.spawn(spawnLocation, template.getLines());
            System.out.println("INFO: Successfully spawned delayed hologram '" + template.getId() + "' at " + spawnLocation.toVector());
        }
    }

    private Location getInitialLocation() {
        if (template.isAttachedToNpc()) {
            NPC npc = CitizensAPI.getNPCRegistry().getById(template.getNpcId());
            if (npc != null && npc.isSpawned()) {
                return npc.getStoredLocation().clone().add(0, template.getOffset(), 0);
            }
        } else {
            // This part handles static holograms
            Location staticLoc = template.getLocation();
            if (staticLoc != null) {
                return staticLoc.clone().add(0, template.getOffset(), 0);
            }
        }
        return null; // Can't determine location yet
    }
}