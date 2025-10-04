package org.nakii.mmorpg.abilities;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.nakii.mmorpg.MMORPGCore;

public class DaggerThrowAbility implements Ability {

    private final MMORPGCore plugin;
    private static final double BASE_DAMAGE = 200;

    public DaggerThrowAbility(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getKey() {
        return "DAGGER_THROW";
    }

    @Override
    public void execute(Player player, ItemStack item) {
        // Create the visual representation of the dagger
        ItemStack daggerItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = daggerItem.getItemMeta();
        daggerItem.setItemMeta(meta);

        // Get the player's direction for the projectile path
        Vector direction = player.getEyeLocation().getDirection().normalize();

        // --- FIX: Spawn the armor stand 1 block in front of the player's eyes ---
        ArmorStand armorStand = player.getWorld().spawn(player.getEyeLocation().add(direction), ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setGravity(false);
            as.setSmall(true);
            as.setMarker(true); // Prevents interaction
            as.getEquipment().setItemInMainHand(daggerItem);
            // Point the sword forward, away from the player
            as.setHeadPose(new EulerAngle(Math.toRadians(90), 0, 0));
        });


        new BukkitRunnable() {
            private int ticksLived = 0;
            private final int maxTicks = 20 * 10; // Max lifetime of 10 seconds (200 ticks)
            private final double speed = 1.5; // Blocks per tick

            @Override
            public void run() {
                // Remove if it has existed too long
                if (ticksLived++ > maxTicks) {
                    armorStand.remove();
                    cancel();
                    return;
                }

                // Move the armor stand forward
                armorStand.teleport(armorStand.getLocation().add(direction.clone().multiply(speed)));
                armorStand.getWorld().spawnParticle(Particle.CRIT, armorStand.getLocation(), 1, 0, 0, 0, 0);

                // Check for nearby living entities (potential targets)
                for (LivingEntity entity : armorStand.getLocation().getNearbyLivingEntities(1.5)) {
                    // Ignore the player who threw it and other players
                    if (entity.equals(player) || entity instanceof Player) {
                        continue;
                    }

                    // --- HIT! ---
                    // Call the new centralized method to handle damage calculation and event firing
                    plugin.getDamageManager().dealAbilityDamage(entity, BASE_DAMAGE, player);

                    // Visual/Audio feedback
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
                    entity.getWorld().spawnParticle(Particle.CRIT, entity.getLocation().add(0, 1, 0), 20);

                    // Clean up and stop the task
                    armorStand.remove();
                    cancel();
                    return; // Exit after hitting the first target
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0f, 1.2f);
    }
}