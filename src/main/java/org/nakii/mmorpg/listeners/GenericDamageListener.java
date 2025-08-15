package org.nakii.mmorpg.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.MetadataValue;
import org.nakii.mmorpg.MMORPGCore;

import java.util.List;

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

            // --- THIS IS THE FIX FOR READING CRITICAL HIT STATUS ---
            boolean wasCrit = false;
            // Check if the metadata tag we set in the other listener exists.
            if (victim.hasMetadata("mmorpg_last_hit_crit")) {
                // Metadata is stored in a list, so we get the first value.
                List<MetadataValue> values = victim.getMetadata("mmorpg_last_hit_crit");
                if (!values.isEmpty()) {
                    // We get the value and convert it to a boolean.
                    wasCrit = values.get(0).asBoolean();
                }
                // It's crucial to remove the metadata tag immediately after reading it
                // so that the next non-critical hit (like fall damage) doesn't use the old value.
                victim.removeMetadata("mmorpg_last_hit_crit", plugin);
            }

            // Only show an indicator if there was actual damage.
            if (event.getFinalDamage() > 0) {
                // Now, we pass the 'wasCrit' boolean we just determined.
                plugin.getHUDManager().showDamageIndicator(victim.getLocation(), event.getFinalDamage(), wasCrit);
            }

            // The logic for updating the custom mob health display remains correct.
            if (plugin.getMobManager().isCustomMob(victim)) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!victim.isDead()) {
                        plugin.getMobManager().updateHealthDisplay(victim);
                    }
                });
            }
        }
    }
}