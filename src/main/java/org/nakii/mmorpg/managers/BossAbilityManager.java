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

public class BossAbilityManager {

    private final MMORPGCore plugin;

    public BossAbilityManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    public void executeAbility(String abilityName, LivingEntity boss) {
        switch (abilityName.toUpperCase()) {
            case "LIFE_DRAIN":
                executeLifeDrain(boss);
                break;
            case "PESTILENCE":
                executePestilence(boss);
                break;
        }
    }

    private void executeLifeDrain(LivingEntity boss) {
        Location loc = boss.getLocation();
        Player target = loc.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(loc) < 225)
                .min((p1, p2) -> Double.compare(p1.getLocation().distanceSquared(loc), p2.getLocation().distanceSquared(loc)))
                .orElse(null);

        if (target != null) {
            double damage = 10;
            double healAmount = damage * 0.5;

            target.damage(damage, boss);
            boss.setHealth(Math.min(boss.getMaxHealth(), boss.getHealth() + healAmount));

            loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SKELETON_HURT, 1.0f, 0.8f);
            target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), (int) damage);
        }
    }

    private void executePestilence(LivingEntity boss) {
        Location loc = boss.getLocation();
        List<Player> nearbyPlayers = loc.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(loc) < 100)
                .toList();

        if (!nearbyPlayers.isEmpty()) {
            loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.5f, 0.5f);
            loc.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, loc.add(0, 1, 0), 50, 2, 1, 2);

            for (Player player : nearbyPlayers) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
                player.damage(15, boss);
            }
        }
    }
}