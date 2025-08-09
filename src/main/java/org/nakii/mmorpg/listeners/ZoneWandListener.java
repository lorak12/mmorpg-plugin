package org.nakii.mmorpg.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.utils.ChatUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the logic for the Zone Wand tool, allowing admins to select
 * the two corners of a new zone.
 */
public class ZoneWandListener implements Listener {

    private final MMORPGCore plugin;
    // These maps will temporarily store the selections for each player.
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();

    public ZoneWandListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Basic validation
        if (item == null || item.getType() == Material.AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return; // Only listen for main hand interaction
        if (!item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "zone_wand"), PersistentDataType.BOOLEAN)) {
            return;
        }

        // Prevent the wand from doing anything else (like breaking blocks)
        event.setCancelled(true);

        Location blockLocation = event.getClickedBlock().getLocation();

        // Left-click for Position 1
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            pos1Map.put(player.getUniqueId(), blockLocation);
            player.sendMessage(ChatUtils.format("<green>Position 1 set to: " + formatLocation(blockLocation) + "</green>"));
        }
        // Right-click for Position 2
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            pos2Map.put(player.getUniqueId(), blockLocation);
            player.sendMessage(ChatUtils.format("<aqua>Position 2 set to: " + formatLocation(blockLocation) + "</aqua>"));
        }
    }

    // Public getters for the command to retrieve the selections
    public Location getPosition1(Player player) {
        return pos1Map.get(player.getUniqueId());
    }

    public Location getPosition2(Player player) {
        return pos2Map.get(player.getUniqueId());
    }

    private String formatLocation(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }
}