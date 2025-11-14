package org.nakii.mmorpg.listeners;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.managers.EnchantmentManager;

import java.util.Map;

public class ProjectileListener implements Listener {

    private final MMORPGCore plugin;
    private final EnchantmentManager enchantmentManager;

    public ProjectileListener(MMORPGCore plugin, EnchantmentManager enchantmentManager) {
        this.plugin = plugin;
        this.enchantmentManager = enchantmentManager;
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getProjectile() instanceof Arrow arrow)) return;

        ItemStack bow = event.getBow();
        if (bow == null) return;

        Map<String, Integer> enchantments = enchantmentManager.getEnchantments(bow);
        if (enchantments.isEmpty()) return;

        arrow.setMetadata("CustomEnchants", new FixedMetadataValue(plugin, enchantments));
        arrow.setMetadata("ShooterUUID", new FixedMetadataValue(plugin, player.getUniqueId()));
    }

    /**
     * --- THIS METHOD NOW CONTAINS THE SNIPE LOGIC ---
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (event.getHitEntity() == null || !(event.getHitEntity() instanceof LivingEntity victim)) return;
        if (!arrow.hasMetadata("CustomEnchants") || !arrow.hasMetadata("ShooterUUID")) return;

        // Retrieve the enchantment data we stored when the arrow was fired.
        Map<String, Integer> enchantments = (Map<String, Integer>) arrow.getMetadata("CustomEnchants").get(0).value();

        Integer snipeLevel = enchantments.get("snipe");
        if (snipeLevel == null || snipeLevel <= 0) return;

        CustomEnchantment snipeEnchant = enchantmentManager.getEnchantment("snipe");
        if (snipeEnchant == null) return;

        // Calculate the distance the arrow traveled.
        double distance = arrow.getLocation().distance(arrow.getOrigin());

        // 'value' is the damage % bonus per 10 blocks.
        double bonusPer10Blocks = snipeEnchant.getValue(snipeLevel) / 100.0;

        // Calculate the damage multiplier.
        double bonusMultiplier = (distance / 10.0) * bonusPer10Blocks;

        // To apply the damage, we find the original damage event this hit will cause.
        // We can do this by waiting one tick and modifying the next damage event for this entity.
        // This is a bit complex. A simpler, more direct way is to just deal extra damage.
        double extraDamage = victim.getLastDamage() * bonusMultiplier;
        victim.damage(extraDamage);
    }
}