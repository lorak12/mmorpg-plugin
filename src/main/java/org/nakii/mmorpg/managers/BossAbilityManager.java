package org.nakii.mmorpg.managers;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.nakii.mmorpg.MMORPGCore;

import java.util.List;

/**
 * Manages and executes hard-coded abilities for Slayer bosses.
 */
public class BossAbilityManager {

    private final MMORPGCore plugin;

    public BossAbilityManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Main dispatcher method. It checks the ability name and calls the correct logic.
     * @param abilityName The name of the ability from slayers.yml (e.g., "LIFE_DRAIN").
     * @param boss The LivingEntity representing the boss.
     */
    public void executeAbility(String abilityName, LivingEntity boss) {
        switch (abilityName.toUpperCase()) {
            case "LIFE_DRAIN":
                executeLifeDrain(boss);
                break;
            case "PESTILENCE":
                executePestilence(boss);
                break;
            // Future abilities like "ENRAGE", "EXPLOSIVE_ASSAULT" would be added here.
        }
    }

    // --- Tier I Ability: Life Drain ---
    private void executeLifeDrain(LivingEntity boss) {
        Location loc = boss.getLocation();
        // Find the nearest player to be the target
        Player target = loc.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(loc) < 225) // 15 block radius
                .min((p1, p2) -> Double.compare(p1.getLocation().distanceSquared(loc), p2.getLocation().distanceSquared(loc)))
                .orElse(null);

        if (target != null) {
            double damage = 10; // Base damage
            double healAmount = damage * 0.5; // Boss heals for 50% of damage dealt

            target.damage(damage, boss);
            boss.setHealth(Math.min(boss.getMaxHealth(), boss.getHealth() + healAmount));

            // Visual and sound effects
            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SKELETON_HURT, 1.0f, 0.8f);
            target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), (int) damage);
        }
    }

    // --- Tier II Ability: Pestilence ---
    private void executePestilence(LivingEntity boss) {
        Location loc = boss.getLocation();
        List<Player> nearbyPlayers = loc.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(loc) < 100) // 10 block radius
                .toList();

        if (!nearbyPlayers.isEmpty()) {
            // Visual and sound effects
            loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.5f, 0.5f);
            loc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, loc.add(0, 1, 0), 50, 2, 1, 2);

            for (Player player : nearbyPlayers) {
                // Apply a "shredding armor" effect via Weakness
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0)); // 5 seconds
                player.damage(15, boss); // Deals AOE damage
            }
        }
    }
}