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
            plugin.getHUDManager().showDamageIndicator(victim.getLocation(), event.getFinalDamage(), false); // Crit is false for now
            if (plugin.getMobManager().getMobLevel(victim) > 0) {
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getMobManager().updateHealthDisplay(victim));
            }
        }
    }
}