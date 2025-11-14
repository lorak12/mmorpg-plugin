package org.nakii.mmorpg.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.MetadataValue;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.managers.HUDManager;
import org.nakii.mmorpg.managers.MobManager;
import org.nakii.mmorpg.util.Keys;

import java.util.List;

public class GenericDamageListener implements Listener {

    private final MMORPGCore plugin;
    private final HUDManager hudManager;
    private final MobManager mobManager;

    public GenericDamageListener(MMORPGCore plugin, HUDManager hudManager, MobManager mobManager) {
        this.plugin = plugin;
        this.hudManager = hudManager;
        this.mobManager = mobManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGenericDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity victim) {
            boolean wasCrit = false;
            if (victim.hasMetadata(Keys.LAST_HIT_CRIT.getKey())) {
                List<MetadataValue> values = victim.getMetadata(Keys.LAST_HIT_CRIT.getKey());
                if (!values.isEmpty()) {
                    wasCrit = values.get(0).asBoolean();
                }
                victim.removeMetadata(Keys.LAST_HIT_CRIT.getKey(), plugin);
            }

            if (event.getFinalDamage() > 0) {
                hudManager.showDamageIndicator(victim.getLocation(), event.getFinalDamage(), wasCrit);
            }

            if (mobManager.isCustomMob(victim)) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!victim.isDead()) {
                        mobManager.updateHealthDisplay(victim);
                    }
                });
            }
        }
    }
}