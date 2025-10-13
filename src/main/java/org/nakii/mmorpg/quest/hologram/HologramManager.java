package org.nakii.mmorpg.quest.hologram;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.QuestManager;
import org.nakii.mmorpg.quest.hologram.ManagedHologram;
import org.nakii.mmorpg.quest.model.QuestHologram;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final MMORPGCore plugin;
    private final QuestManager questManager;
    private final Map<String, ManagedHologram> managedHolograms = new ConcurrentHashMap<>();

    private BukkitTask visibilityTask;
    private BukkitTask followTask;

    public HologramManager(MMORPGCore plugin) {
        this.plugin = plugin;
        this.questManager = plugin.getQuestManager();
    }

    public void initialize() {
        // Load all hologram configurations from quest files
        loadAllHolograms();

        // Start the tasks that will manage them
        startTasks();
    }

    public void shutdown() {
        // Stop the tasks
        if (visibilityTask != null) visibilityTask.cancel();
        if (followTask != null) followTask.cancel();

        // Cleanly delete all managed holograms
        managedHolograms.values().forEach(ManagedHologram::destroy);
        managedHolograms.clear();
    }

    private void loadAllHolograms() {
        shutdown(); // Ensure we're starting fresh
        plugin.getLogger().info("Loading all quest holograms...");

        for (QuestHologram template : questManager.getAllHolograms()) {
            ManagedHologram managedHologram = new ManagedHologram(template);
            managedHolograms.put(managedHologram.getId(), managedHologram);
        }

        plugin.getLogger().info("Loaded " + managedHolograms.size() + " managed holograms.");
    }

    private void startTasks() {
        // This task checks conditions and toggles visibility. Runs less frequently.
        visibilityTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) return;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (ManagedHologram hologram : managedHolograms.values()) {
                        boolean conditionsMet = questManager.checkConditions(player, hologram.getTemplate().getConditions());
                        hologram.updateVisibilityFor(player, conditionsMet);
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 40L); // Start after 3s, repeat every 2s

        // This task makes holograms follow NPCs. Runs every tick for smoothness.
        followTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (ManagedHologram hologram : managedHolograms.values()) {
                    if (hologram.getTemplate().isAttachedToNpc()) {
                        hologram.updatePosition();
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 1L); // Start after 3s, repeat every tick
    }

    public void onPlayerQuit(Player player) {
        // Clean up the player's data from all holograms to prevent memory leaks
        for (ManagedHologram hologram : managedHolograms.values()) {
            hologram.removePlayer(player);
        }
    }

    public void reload() {
        plugin.getLogger().info("Reloading all quest holograms...");
        // The shutdown method already deletes all holograms and cancels tasks.
        shutdown();
        // The initialize method already loads all holograms and starts the tasks.
        initialize();
        plugin.getLogger().info("Hologram reload complete.");
    }
}