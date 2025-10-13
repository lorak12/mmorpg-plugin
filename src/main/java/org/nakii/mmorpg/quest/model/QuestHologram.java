package org.nakii.mmorpg.quest.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a template for a per-player quest hologram loaded from a quest file.
 */
public class QuestHologram {

    private final String id;
    private final Location location; // This can now be null for NPC-attached holograms
    private final double offset;
    private final List<String> lines;
    private final List<String> conditions;
    private final int npcId;

    public QuestHologram(String id, Map<String, Object> data) {
        this.id = id;
        this.npcId = (Integer) data.getOrDefault("npcId", -1);
        this.offset = ((Number) data.getOrDefault("offset", 0.0)).doubleValue();
        this.lines = (List<String>) data.getOrDefault("lines", Collections.singletonList("<yellow>!"));
        this.conditions = (List<String>) data.getOrDefault("conditions", Collections.emptyList());

        // --- THIS IS THE CORRECTED LOGIC ---
        // We only parse a static location if the hologram is NOT attached to an NPC.
        if (!isAttachedToNpc()) {
            String worldName = (String) data.get("world");
            // Gracefully handle missing required fields for static holograms
            if (worldName == null || !data.containsKey("x") || !data.containsKey("y") || !data.containsKey("z")) {
                Bukkit.getLogger().severe("[MMORPGCore] Hologram '" + id + "' is not attached to an NPC but is missing required location fields (world, x, y, z). It will not be loaded.");
                this.location = null;
                return;
            }

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                Bukkit.getLogger().severe("[MMORPGCore] Hologram '" + id + "' specifies an unknown world: '" + worldName + "'. It will not be loaded.");
                this.location = null;
                return;
            }

            double x = ((Number) data.get("x")).doubleValue();
            double y = ((Number) data.get("y")).doubleValue();
            double z = ((Number) data.get("z")).doubleValue();
            this.location = new Location(world, x, y, z);
        } else {
            // If it's attached to an NPC, the location will be determined dynamically.
            this.location = null;
        }
    }

    public String getId() {
        return id;
    }

    /**
     * For static holograms, returns the configured location.
     * For NPC-attached holograms, this will be null.
     * @return A clone of the location, or null.
     */
    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    public double getOffset() {
        return offset;
    }

    public List<String> getLines() {
        return lines;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public int getNpcId() {
        return npcId;
    }

    public boolean isAttachedToNpc() {
        return npcId != -1;
    }
}