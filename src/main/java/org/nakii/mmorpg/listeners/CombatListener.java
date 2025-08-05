package org.nakii.mmorpg.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.Skill;

public class CombatListener implements Listener {

    private final MMORPGCore plugin;

    public CombatListener(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity) || !(event.getDamager() instanceof LivingEntity)) {
            return;
        }

        LivingEntity victim = (LivingEntity) event.getEntity();
        LivingEntity attacker = (LivingEntity) event.getDamager();

        // Grant XP if the attacker is a player
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                plugin.getSkillManager().addExperience(player, Skill.ARCHERY, event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE));
            } else {
                plugin.getSkillManager().addExperience(player, Skill.COMBAT, event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE));
            }
        }

        plugin.getDamageManager().handleDamage(attacker, victim, event.getCause());
        event.setDamage(0);
    }
}