package org.nakii.mmorpg.quest.engine.event;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.engine.EventFactory;
import org.nakii.mmorpg.quest.model.QuestPackage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A special event that executes a list of other events, with optional timing controls.
 */
public class FolderEvent implements QuestEvent {

    private final List<String> childEventStrings;
    private final Map<String, String> parameters;

    public FolderEvent(List<String> childEventStrings, Map<String, String> parameters) {
        this.childEventStrings = childEventStrings;
        this.parameters = parameters;
    }

    @Override
    public void execute(Player player, MMORPGCore plugin, QuestPackage context) {
        // --- Handle Randomization ---
        int randomCount = Integer.parseInt(parameters.getOrDefault("random", "0"));
        if (randomCount > 0) {
            Collections.shuffle(childEventStrings);
            // Trim the list to the number of random events to execute
            List<String> randomizedEvents = childEventStrings.subList(0, Math.min(randomCount, childEventStrings.size()));
            executeEvents(randomizedEvents, player, plugin, context);
            return;
        }

        // --- Handle Timing Controls ---
        long delay = Long.parseLong(parameters.getOrDefault("delay", "0"));
        long period = Long.parseLong(parameters.getOrDefault("period", "0"));

        // Convert minutes to ticks if specified
        if (parameters.containsKey("minutes")) {
            delay *= 1200; // 60 seconds * 20 ticks
        }

        // --- Execute Based on Timing ---
        if (period > 0) {
            // This is a repeating task
            new BukkitRunnable() {
                final AtomicInteger executionCount = new AtomicInteger(0);
                @Override
                public void run() {
                    executeEvents(childEventStrings, player, plugin, context); // Pass context
                }
            }.runTaskTimer(plugin, delay, period);

        } else if (delay > 0) {
            // This is a delayed task
            new BukkitRunnable() {
                @Override
                public void run() {
                    executeEvents(childEventStrings, player, plugin, context); // Pass context
                }
            }.runTaskLater(plugin, delay);

        } else {
            // Execute immediately
            executeEvents(childEventStrings, player, plugin, context);
        }
    }

    /**
     * Helper method to execute a list of event strings for a player.
     */
    private void executeEvents(List<String> eventStrings, Player player, MMORPGCore plugin, QuestPackage context) {
        for (String eventString : eventStrings) {
            // FIX: Pass the context package to the centralized execution method
            plugin.getQuestManager().executeEvent(player, eventString, context);
        }
    }
}