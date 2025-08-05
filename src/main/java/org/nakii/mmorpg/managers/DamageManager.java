package org.nakii.mmorpg.managers;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.stats.PlayerStats;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

public class DamageManager {

    private final MMORPGCore plugin;
    private static final DecimalFormat df = new DecimalFormat("#.##");

    public DamageManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void handleDamage(LivingEntity attacker, LivingEntity victim, EntityDamageEvent.DamageCause cause) {
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && cause != EntityDamageEvent.DamageCause.PROJECTILE) {
            return;
        }

        PlayerStats attackerStats = getEntityStats(attacker);
        PlayerStats victimStats = getEntityStats(victim);

        double damage = attackerStats.getStrength();
        boolean isCrit = ThreadLocalRandom.current().nextDouble(100) < attackerStats.getCritChance();
        if (isCrit) {
            damage *= 1 + (attackerStats.getCritDamage() / 100.0);
        }

        double defense = victimStats.getArmor();
        double armorPen = attackerStats.getArmorPenetration();
        double effectiveArmor = Math.max(0, defense * (1 - (armorPen / 100.0)));
        double damageReduction = effectiveArmor / (effectiveArmor + 100);
        damage *= (1 - damageReduction);

        applyFinalDamage(victim, damage, isCrit);
    }

    private void applyFinalDamage(LivingEntity victim, double damage, boolean isCrit) {
        HealthManager healthManager = plugin.getHealthManager();
        double currentHealth = healthManager.getCurrentHealth(victim);
        double newHealth = currentHealth - damage;

        healthManager.setCurrentHealth(victim, newHealth);
        spawnDamageIndicator(victim.getLocation(), damage, isCrit);

        if (!(victim instanceof Player)) {
            updateMobHealthDisplay(victim);
        }

        if (newHealth <= 0) {
            victim.setHealth(0); // Trigger the vanilla death event
        }
    }

    private PlayerStats getEntityStats(LivingEntity entity) {
        if (entity instanceof Player) {
            return plugin.getStatsManager().getStats((Player) entity);
        }
        // TODO: Implement a real mob stats system
        return new PlayerStats(); // Return base stats for mobs for now
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

    public void updateMobHealthDisplay(LivingEntity mob) {
        HealthManager healthManager = plugin.getHealthManager();
        double current = healthManager.getCurrentHealth(mob);
        double max = healthManager.getMaxHealth(mob);

        String name = mob.getType().name();
        mob.setCustomName(ChatColor.WHITE + name + " " + ChatColor.RED + "[" + df.format(current) + "/" + df.format(max) + "]");
        mob.setCustomNameVisible(true);
    }
}