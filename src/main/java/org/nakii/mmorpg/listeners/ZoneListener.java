package org.nakii.mmorpg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.PlayerStateManager;
import org.nakii.mmorpg.tasks.ClimateTask;
import org.nakii.mmorpg.zone.PlayerZoneTracker;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.requirements.Requirement;
import org.nakii.mmorpg.zone.Zone;
import org.nakii.mmorpg.events.PlayerZoneChangeEvent;

import java.time.Duration;
import java.util.List;


public class ZoneListener implements Listener {

    private final PlayerZoneTracker zoneTracker;
    private final PlayerStateManager stateManager;
    private final ClimateTask climateTask;

    public ZoneListener(MMORPGCore plugin) {
        this.zoneTracker = plugin.getPlayerZoneTracker();
        this.stateManager = plugin.getPlayerStateManager();
        this.climateTask = plugin.getClimateTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        stateManager.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        zoneTracker.removePlayer(event.getPlayer());
        stateManager.removePlayer(event.getPlayer());
        climateTask.removeDebugger(event.getPlayer());
    }

    @EventHandler
    public void onZoneChange(PlayerZoneChangeEvent event) {
        Zone toZone = event.getToZone();
        Player player = event.getPlayer();

        if (toZone == null) {
            return; // Player left a zone into the wilderness
        }

        // --- 1. Handle Entry Restrictions ---
        List<String> reqStrings = toZone.getEffectiveFlags().entryRequirements();
        if (reqStrings != null && !reqStrings.isEmpty()) {
            for (String reqString : reqStrings) {
                Requirement requirement = Requirement.fromString(reqString);
                if (requirement == null) {
                    System.err.println("Invalid requirement string in zone '" + toZone.getId() + "': " + reqString);
                    continue;
                }

                if (!requirement.meets(player)) {
                    // Requirement failed, teleport player back and send a message.
                    player.teleport(event.getFromLocation());
                    player.sendMessage(generateFailureMessage(reqString));
                    return; // Stop processing immediately
                }
            }
        }

        // --- 2. Handle First-Time Discovery ---
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

    /**
     * Generates a user-friendly message for a failed requirement string.
     * @param reqString The raw requirement string (e.g., "SKILL:MINING:5").
     * @return A formatted Component to send to the player.
     */
    private Component generateFailureMessage(String reqString) {
        String[] parts = reqString.split(":");
        if (parts.length < 3) {
            return Component.text("You do not meet the requirements for this area.", NamedTextColor.RED);
        }

        String type = parts[0];
        String context = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1).toLowerCase();
        String value = parts[2];

        String message = switch (type.toUpperCase()) {
            case "SKILL" -> "<red>You must have " + context + " Level " + value + " to enter this area.";
            default -> "<red>You do not meet the hidden requirements for this area.";
        };

        return MiniMessage.miniMessage().deserialize(message);
    }
}