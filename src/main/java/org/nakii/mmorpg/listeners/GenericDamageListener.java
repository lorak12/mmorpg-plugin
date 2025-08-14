package org.nakii.mmorpg.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.nakii.mmorpg.MMORPGCore;

public class GenericDamageListener implements Listener {

    private final MMORPGCore plugin;

    public GenericDamageListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * --- FIX for Bug #4: Environmental Damage Health Update ---
     * Listens to all damage events at a low priority.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGenericDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity victim) {
            // --- NEW: Read the crit status from metadata ---
            boolean wasCrit = false;
            if (victim.hasMetadata("last_hit_crit")) {
                // The metadata value is a list, get the first one.
                wasCrit = victim.getMetadata("last_hit_crit").get(0).asBoolean();
                // Important: Remove the metadata so it's fresh for the next hit.
                victim.removeMetadata("last_hit_crit", plugin);
            }

            // Only show an indicator if there was actual damage
            if (event.getFinalDamage() > 0) {
                plugin.getHUDManager().showDamageIndicator(victim.getLocation(), event.getFinalDamage(), wasCrit);
            }

            // Update the health display for our custom mobs
            if (plugin.getMobManager().getMobLevel(victim) > 0) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!victim.isDead()) { // Check if the mob didn't die from this hit
                        plugin.getMobManager().updateHealthDisplay(victim);
                    }
                });
            }
        }
    }
}