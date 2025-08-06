package org.nakii.mmorpg.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.entity.CustomMob;
import org.nakii.mmorpg.utils.ChatUtils;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

public class DamageManager {

    private final MMORPGCore plugin;
    private static final DecimalFormat df = new DecimalFormat("#.#");

    public DamageManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void handleDamage(LivingEntity attacker, LivingEntity victim, EntityDamageEvent.DamageCause cause) {
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && cause != EntityDamageEvent.DamageCause.PROJECTILE) return;

        double attackerStrength = (attacker instanceof Player) ? plugin.getStatsManager().getStats((Player) attacker).getStrength() : plugin.getMobManager().getMobStrength(attacker);

        double damage = attackerStrength; // Simplified for now

        HealthManager healthManager = plugin.getHealthManager();
        double currentHealth = healthManager.getCurrentHealth(victim);
        double newHealth = currentHealth - damage;
        healthManager.setCurrentHealth(victim, newHealth);

        spawnDamageIndicator(victim.getLocation(), damage, false);

        // Update name display for BOTH custom and natural mobs with health
        updateMobHealthDisplay(victim);

        if (newHealth <= 0) {
            victim.setHealth(0);
        }
    }

    /**
     * Centralized method to update the name display of ANY living entity with health values.
     */
    public void updateMobHealthDisplay(LivingEntity mob) {
        if (!mob.isValid()) return;

        double current = plugin.getHealthManager().getCurrentHealth(mob);
        double max = plugin.getHealthManager().getMaxHealth(mob);
        String name;

        if (plugin.getMobManager().isCustomMob(mob)) {
            // Logic for Custom Mobs
            CustomMob customMob = plugin.getMobManager().getCustomMob(plugin.getMobManager().getMobId(mob));
            if (customMob == null) return;
            name = "<gray>[Lv. " + customMob.getConfig().getInt("level") + "] " + customMob.getDisplayName() + " <red>[" + df.format(current) + "/" + df.format(max) + "]";
        } else {
            // Logic for Natural Mobs
            // FINAL FIX: Use mob.getType().name() to always get the base entity type, avoiding the feedback loop.
            String originalName = mob.getType().name();

            // FINAL FIX: Corrected the getOrDefault syntax. The type comes before the default value.
            int level = mob.getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "mob_level"), PersistentDataType.INTEGER, 1);

            name = "<gray>[Lv. " + level + "] <white>" + originalName + " <red>[" + df.format(current) + "/" + df.format(max) + "]";
        }

        // Use the modern Component API to apply the formatted name.
        mob.customName(ChatUtils.format(name));
        mob.setCustomNameVisible(true);
    }


    private void spawnDamageIndicator(Location loc, double damage, boolean isCrit) {
        Location spawnLoc = loc.clone().add(
                ThreadLocalRandom.current().nextDouble(-0.5, 0.5), 1.0, ThreadLocalRandom.current().nextDouble(-0.5, 0.5));
        ArmorStand armorStand = spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setCustomNameVisible(true);

        String damageText = (isCrit ? ChatColor.YELLOW + "" + ChatColor.BOLD + "✧ " : ChatColor.RED + "") + df.format(damage);
        armorStand.setCustomName(damageText);

        new BukkitRunnable() {
            @Override
            public void run() {
                armorStand.remove();
            }
        }.runTaskLater(plugin, 30L);
    }
}