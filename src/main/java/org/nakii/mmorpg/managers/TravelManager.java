package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.world.CustomWorld;
import org.nakii.mmorpg.zone.Zone;

import java.util.Optional;

public class TravelManager {

    private final MMORPGCore plugin;
    private final WorldManager worldManager;
    private final RequirementManager requirementManager;

    public TravelManager(MMORPGCore plugin, WorldManager worldManager, RequirementManager requirementManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.requirementManager = requirementManager;
    }

    /**
     * Teleports a player to a specific CustomWorld's spawn point.
     * @param player The player to teleport.
     * @param worldName The name of the target world.
     */
    public void travelToWorld(Player player, String worldName) {
        CustomWorld world = worldManager.getCustomWorld(worldName);
        if (world == null) {
            player.sendMessage(Component.text("The world '" + worldName + "' does not exist.", NamedTextColor.RED));
            return;
        }

        // Check world entry requirements
        if (!requirementManager.meetsAll(player, world.getFlags().entryRequirements())) {
            player.sendMessage(Component.text("You do not meet the requirements to travel to this world.", NamedTextColor.RED));
            return;
        }

        Optional<Location> spawnOpt = world.getSpawnPoint();
        if (spawnOpt.isEmpty()) {
            player.sendMessage(Component.text("The world '" + worldName + "' does not have a spawn point set.", NamedTextColor.RED));
            return;
        }

        player.teleportAsync(spawnOpt.get());
        player.sendMessage(Component.text("Traveled to " + worldName + ".", NamedTextColor.GREEN));
    }

    /**
     * Teleports a player to a specific Zone's warp point.
     * @param player The player to teleport.
     * @param worldName The world the zone is in.
     * @param zoneId The ID of the target zone.
     */
    public void travelToZone(Player player, String worldName, String zoneId) {
        CustomWorld world = worldManager.getCustomWorld(worldName);
        if (world == null) {
            player.sendMessage(Component.text("The world '" + worldName + "' does not exist.", NamedTextColor.RED));
            return;
        }

        Zone zone = world.getZoneById(zoneId);
        if (zone == null) {
            player.sendMessage(Component.text("The zone '" + zoneId + "' does not exist in " + worldName + ".", NamedTextColor.RED));
            return;
        }

        // Check zone entry requirements
        if (!requirementManager.meetsAll(player, zone.getFlags().entryRequirements())) {
            player.sendMessage(Component.text("You do not meet the requirements to travel to this zone.", NamedTextColor.RED));
            return;
        }

        Optional<Location> warpOpt = zone.getWarpPoint();
        if (warpOpt.isEmpty()) {
            player.sendMessage(Component.text("The zone '" + zoneId + "' does not have a warp point set.", NamedTextColor.RED));
            return;
        }

        player.teleportAsync(warpOpt.get());
        player.sendMessage(Component.text("Warped to " + zoneId + ".", NamedTextColor.GREEN));
    }
}