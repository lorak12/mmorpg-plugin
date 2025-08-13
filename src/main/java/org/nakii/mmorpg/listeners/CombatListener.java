package org.nakii.mmorpg.listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.nakii.mmorpg.MMORPGCore;

public class CombatListener implements Listener {

    private final MMORPGCore plugin;
    private static final double MAX_PLAYER_REACH = 3.5;

    public CombatListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * The single, unified damage handler for the entire plugin.
     * It intercepts all damage to managed entities and processes it exactly once.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnyDamage(EntityDamageEvent event) {
        // --- 1. Initial validation ---
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) event.getEntity();
        LivingEntity attacker = null;
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            if (entityEvent.getDamager() instanceof LivingEntity) {
                attacker = (LivingEntity) entityEvent.getDamager();
            }
        }

        boolean isVictimManaged = plugin.getHealthManager().isManagedEntity(victim);
        boolean isAttackerManaged = attacker != null && plugin.getHealthManager().isManagedEntity(attacker);

        if (!isVictimManaged && !isAttackerManaged) return;

        // --- 2. Calculate Final Custom Damage ---
        double customDamage;
        boolean isCrit = false; // We need to pass this to the indicator
        if (attacker != null) {
            if (attacker instanceof Player) {
                if (((Player) attacker).getAttackCooldown() < 1.0 || attacker.getLocation().distance(victim.getLocation()) > MAX_PLAYER_REACH) {
                    event.setCancelled(true);
                    return;
                }
            }
            // We can't just get the damage number, we need the crit status too. Let's get it from the manager.
            customDamage = plugin.getDamageManager().calculateAttackDamage(attacker, victim);
            isCrit = plugin.getDamageManager().wasLastAttackCrit(attacker); // We will add this helper method
        } else {
            customDamage = event.getFinalDamage();
        }

        // --- 3. DAMAGE INDICATOR FIX: Spawn the indicator right after calculating damage ---
        plugin.getDamageManager().spawnDamageIndicator(victim.getLocation(), customDamage, isCrit);

        // --- 4. MOB DAMAGE FIX: Use a UNIFIED health scaling approach for ALL entities ---
        double maxCustomHealth = plugin.getHealthManager().getMaxHealth(victim);
        double vanillaMaxHealth = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double scaledVanillaDamage = (customDamage / maxCustomHealth) * vanillaMaxHealth;

        plugin.getHealthManager().applyDamage(victim, customDamage);
        event.setDamage(scaledVanillaDamage);

        // --- 5. Update mob health display ---
        if (!(victim instanceof Player)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getDamageManager().updateMobHealthDisplay(victim));
        }
    }



    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        plugin.getHealthManager().unregisterEntity(event.getEntity());

        // If the killer is a player, grant them Combat XP
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            plugin.getSkillManager().handleMobKill(killer, event.getEntityType());
        }

        if (plugin.getMobManager().isCustomMob(event.getEntity())) {
            plugin.getZoneManager().untrackMob(event.getEntity());
        }
    }
}