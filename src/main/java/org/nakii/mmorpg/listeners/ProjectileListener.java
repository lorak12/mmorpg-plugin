package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.nakii.mmorpg.MMORPGCore;

import java.util.Map;

public class ProjectileListener implements Listener {

    private final MMORPGCore plugin;

    public ProjectileListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    // This event fires when a player shoots a bow.
    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getProjectile() instanceof Arrow arrow)) return;

        ItemStack bow = event.getBow();
        if (bow == null) return;

        Map<String, Integer> enchantments = plugin.getEnchantmentManager().getEnchantments(bow);
        if (enchantments.isEmpty()) return;

        // "Tag" the arrow with the bow's enchantments by storing them in its metadata.
        // This makes the enchant data available when the arrow hits.
        arrow.setMetadata("CustomEnchants", new FixedMetadataValue(plugin, enchantments));
    }

    // This event fires when any projectile hits something.
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (event.getHitEntity() == null) return; // We only care about hits on entities

        if (arrow.hasMetadata("CustomEnchants")) {
            // Here you could implement Snipe by calculating distance.
            // For now, we'll let the PlayerDamageListener handle Overload.
            // This listener is ready for future projectile-specific enchantments.
        }
    }
}