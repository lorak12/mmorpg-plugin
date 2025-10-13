package org.nakii.mmorpg.quest;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.quest.hider.CitizensHider;
import org.nakii.mmorpg.quest.model.NPCVisibilityRule;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages NPC visibility per player using CitizensHider
 */
public class NPCVisibilityManager {

    private final MMORPGCore plugin;
    private final QuestManager questManager;
    private final boolean debug = false;

    public NPCVisibilityManager(MMORPGCore plugin, QuestManager questManager) {
        this.plugin = plugin;
        this.questManager = questManager;
    }

    /**
     * Initialize the repeating visibility check task
     */
    public void initialize() {
        new VisibilityUpdateTask().runTaskTimer(plugin, 80L, 20L);
    }

    /**
     * Called when a player joins
     */
    public void onPlayerJoin(Player player) {
        // Force update twice in case Citizens spawns late
        Bukkit.getScheduler().runTaskLater(plugin, () -> forceFullUpdateForPlayer(player), 20L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> forceFullUpdateForPlayer(player), 100L);
    }

    /**
     * Forces full visibility update for a specific player
     */
    public void forceFullUpdateForPlayer(Player player) {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        if (registry == null) return;

        for (NPCVisibilityRule rule : questManager.getAllVisibilityRules()) {
            NPC npc = registry.getById(rule.getNpcId());
            if (npc == null || !npc.isSpawned()) continue;

            boolean shouldBeVisible = questManager.checkConditions(player, rule.getConditions());
            updateVisibility(player, npc, shouldBeVisible);
        }
    }

    /**
     * BukkitRunnable repeating task for updating all players
     */
    private class VisibilityUpdateTask extends BukkitRunnable {
        @Override
        public void run() {
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            if (registry == null) return;

            for (Player player : Bukkit.getOnlinePlayers()) {
                for (NPCVisibilityRule rule : questManager.getAllVisibilityRules()) {
                    NPC npc = registry.getById(rule.getNpcId());
                    if (npc == null || !npc.isSpawned()) continue;

                    boolean shouldBeVisible = questManager.checkConditions(player, rule.getConditions());
                    updateVisibility(player, npc, shouldBeVisible);
                }
            }
        }
    }

    /**
     * Updates visibility of an NPC for a player using CitizensHider
     */
    private void updateVisibility(Player player, NPC npc, boolean shouldBeVisible) {
        boolean isCurrentlyVisible = CitizensHider.getInstance().isInvisible(player, npc) == false;

        if (shouldBeVisible && !isCurrentlyVisible) {
            if (debug) plugin.getLogger().info("[NPCVisibility] Showing npc " + npc.getId() + " to " + player.getName());
            CitizensHider.getInstance().show(player, npc);
        } else if (!shouldBeVisible && isCurrentlyVisible) {
            if (debug) plugin.getLogger().info("[NPCVisibility] Hiding npc " + npc.getId() + " from " + player.getName());
            CitizensHider.getInstance().hide(player, npc);
        }
    }
}
