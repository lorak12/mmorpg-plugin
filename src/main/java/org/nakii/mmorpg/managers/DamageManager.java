package org.nakii.mmorpg.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.entity.CustomMob;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.stats.PlayerStats;
import org.nakii.mmorpg.utils.ChatUtils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DamageManager {

    private final MMORPGCore plugin;
    private static final DecimalFormat df = new DecimalFormat("#.#");
    private final Map<UUID, Boolean> lastAttackCritMap = new HashMap<>();

    public DamageManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }




    public void processGenericDamage(LivingEntity victim, double damage) {
        // For now, generic damage ignores defense. This can be changed.
        spawnDamageIndicator(victim.getLocation(), damage, false);
        plugin.getHealthManager().applyDamage(victim, damage);

        applyDamageAndFeedback(victim, damage, false);
    }

    /**
     * Helper method to get a PlayerStats object for any LivingEntity.
     */
    private PlayerStats getEntityStats(LivingEntity entity) {
        if (entity instanceof Player) {
            return plugin.getStatsManager().getStats((Player) entity);
        }

        PlayerStats stats = new PlayerStats(true); // Create empty stats
        if (plugin.getMobManager().isCustomMob(entity)) {
            ConfigurationSection mobStats = plugin.getMobManager().getCustomMob(plugin.getMobManager().getMobId(entity)).getStatsConfig();
            stats.setDamage(mobStats.getDouble("stats.damage", 3.0));
            stats.setHealth(mobStats.getDouble("stats.health", 20.0));
            stats.setDefense(mobStats.getDouble("stats.defense", 0.0));
            stats.setStrength(mobStats.getDouble("stats.strength", 0.0));
        } else {
            // For vanilla mobs
            stats.setDamage(entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null ? entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() : 1.0);
            stats.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null ? entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 20.0);
            stats.setDefense(entity.getAttribute(Attribute.GENERIC_ARMOR) != null ? entity.getAttribute(Attribute.GENERIC_ARMOR).getValue() : 0.0);
            stats.setStrength(0);
        }
        return stats;
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

            name = "<gray>[Lv. " + level + "] <white>" + ChatUtils.capitalizeWords(originalName) + " <red>[" + df.format(current) + "/" + df.format(max) + "]";
        }

        // Use the modern Component API to apply the formatted name.
        mob.customName(ChatUtils.format(name));
        mob.setCustomNameVisible(true);
    }

    public void spawnDamageIndicator(Location loc, double damage, boolean isCrit) {
        Location spawnLoc = loc.clone().add(
                ThreadLocalRandom.current().nextDouble(-0.5, 0.5), 1.0, ThreadLocalRandom.current().nextDouble(-0.5, 0.5));
        ArmorStand armorStand = spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setMarker(true);
        armorStand.setCustomNameVisible(true);

        String damageText = (isCrit ? "<gold><b>✧ " : "<red>") + df.format(damage);
        armorStand.customName(ChatUtils.format(damageText));

        new BukkitRunnable() {
            @Override
            public void run() {
                armorStand.remove();
            }
        }.runTaskLater(plugin, 30L);
    }

    /**
     * The new, central method for applying all damage effects and feedback.
     */
    public void applyDamageAndFeedback(LivingEntity victim, double damage, boolean isCrit) {
        // 1. Apply the health change.
        plugin.getHealthManager().applyDamage(victim, damage);

        // 2. Spawn the visual damage indicator.
        spawnDamageIndicator(victim.getLocation(), damage, isCrit);

        // 3. Update the health display name for the victim, but only if it's not a player.
        // A small delay ensures this happens after the health has been updated.
        if (!(victim instanceof Player)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> updateMobHealthDisplay(victim));
        }
    }

    /**
     * Calculates the final custom damage from an attack. Does NOT apply it.
     * @return The final calculated custom damage.
     */
    public double calculateAttackDamage(LivingEntity attacker, LivingEntity victim) {
        PlayerStats attackerStats = getEntityStats(attacker);
        PlayerStats victimStats = getEntityStats(victim);
        double defense = victimStats.getDefense();
        double defenseMultiplier = 100.0 / (100.0 + defense);
        double baseDamage = attackerStats.getDamage();
        double strengthMultiplier = 1 + (attackerStats.getStrength() / 100.0);
        double damageAfterStrength = baseDamage * strengthMultiplier;
        double critMultiplier = 1.0;
        if (ThreadLocalRandom.current().nextDouble(100) < attackerStats.getCritChance()) {
            critMultiplier = 1 + (attackerStats.getCritDamage() / 100.0);
        }
        boolean isCrit = false; // reset
        if (ThreadLocalRandom.current().nextDouble(100) < attackerStats.getCritChance()) {
            isCrit = true;
            critMultiplier = 1 + (attackerStats.getCritDamage() / 100.0);
        }
        // Store the result for the listener to use
        lastAttackCritMap.put(attacker.getUniqueId(), isCrit);

        double finalDamage = damageAfterStrength * critMultiplier * defenseMultiplier;
        return Math.max(1.0, Math.round(finalDamage));
    }

    /**
     * Helper method for the listener to know if the last calculated attack was a crit.
     */
    public boolean wasLastAttackCrit(LivingEntity attacker) {
        return lastAttackCritMap.getOrDefault(attacker.getUniqueId(), false);
    }


}