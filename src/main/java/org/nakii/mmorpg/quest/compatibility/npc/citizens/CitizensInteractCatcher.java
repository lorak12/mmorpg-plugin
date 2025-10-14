package org.nakii.mmorpg.quest.compatibility.npc.citizens;

import net.citizensnpcs.api.event.*;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.nakii.mmorpg.quest.api.bukkit.event.npc.NpcVisibilityUpdateEvent;
import org.nakii.mmorpg.quest.api.profile.ProfileProvider;
import org.nakii.mmorpg.quest.api.quest.npc.NpcRegistry;
import org.nakii.mmorpg.quest.api.quest.npc.feature.NpcInteractCatcher;
import org.nakii.mmorpg.quest.quest.objective.interact.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.function.Predicate;

/**
 * Catches Citizens NPC interactions and adapts them into the BetonQuest event.
 */
public class CitizensInteractCatcher extends NpcInteractCatcher<NPC> {
    /**
     * Source Registry of NPCs to consider.
     */
    private final NPCRegistry registry;

    /**
     * Move Controller to check if the NPC blocks conversations while moving.
     */
    private final Predicate<NPC> cancelPredicate;

    /**
     * Initializes the catcher for Citizens.
     *
     * @param profileProvider the profile provider instance
     * @param npcRegistry     the registry to identify the clicked Npc
     * @param registry        the registry of NPCs to notice interactions
     * @param cancelPredicate the move predicate to check if the NPC currently blocks conversations
     *                        if the predicate test yields 'true' the adapted event will be fired cancelled
     */
    public CitizensInteractCatcher(final ProfileProvider profileProvider, final NpcRegistry npcRegistry,
                                   final NPCRegistry registry, final Predicate<NPC> cancelPredicate) {
        super(profileProvider, npcRegistry);
        this.registry = registry;
        this.cancelPredicate = cancelPredicate;
    }

    private void interactLogic(final NPCClickEvent event, final Interaction interaction) {
        final NPC npc = event.getNPC();
        if (!npc.getOwningRegistry().equals(registry)) {
            return;
        }
        if (super.interactLogic(event.getClicker(), new CitizensAdapter(npc), interaction,
                cancelPredicate.test(npc), event.isAsynchronous())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles right clicks.
     *
     * @param event the event to handle
     */
    @EventHandler(ignoreCancelled = true)
    public void onNPCClick(final NPCRightClickEvent event) {
        interactLogic(event, Interaction.RIGHT);
    }

    /**
     * Handles left click.
     *
     * @param event the event to handle
     */
    @EventHandler(ignoreCancelled = true)
    public void onNPCClick(final NPCLeftClickEvent event) {
        interactLogic(event, Interaction.LEFT);
    }

    /**
     * Update the hologram when the plugin reloads.
     *
     * @param event The event.
     */
    @EventHandler
    public void onCitizensReload(final CitizensReloadEvent event) {
        new NpcVisibilityUpdateEvent(null).callEvent();
    }

    /**
     * Update the hologram when the NPC spawns.
     *
     * @param event The event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCSpawn(final NPCSpawnEvent event) {
        updateHologram(event.getNPC());
    }

    /**
     * Update the hologram when the NPC despawns.
     *
     * @param event The event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCDespawn(final NPCDespawnEvent event) {
        updateHologram(event.getNPC());
    }

    /**
     * Update the hologram when the NPC moves.
     *
     * @param event The event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCTeleport(final NPCTeleportEvent event) {
        updateHologram(event.getNPC());
    }

    private void updateHologram(final NPC npc) {
        if (npc.getOwningRegistry().equals(registry)) {
            new NpcVisibilityUpdateEvent(new CitizensAdapter(npc)).callEvent();
        }
    }
}
