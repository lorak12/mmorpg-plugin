package org.nakii.mmorpg.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.enchantment.effects.EnchantmentEffect;
import org.nakii.mmorpg.managers.*;
import org.nakii.mmorpg.player.PlayerStats;

import java.util.Map;

public class PlayerDamageListener implements Listener {

    private final MMORPGCore plugin;
    private final CombatTracker combatTracker;
    private final EnchantmentManager enchantmentManager;
    private final StatsManager statsManager;
    private final DamageManager damageManager;

    public PlayerDamageListener(MMORPGCore plugin) {
        this.plugin = plugin;
        this.combatTracker = plugin.getCombatTracker();
        this.enchantmentManager = plugin.getEnchantmentManager();
        this.statsManager = plugin.getStatsManager();
        this.damageManager = plugin.getDamageManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        double finalDamage = event.getDamage();
        boolean isCustomCrit = false;

        // Neutralize vanilla critical damage so we can apply our own
        if (event.isCritical()) {
            finalDamage /= 1.5;
        }

        // --- Phase 1: Attacker Logic ---
        if (event.getDamager() instanceof Player attacker) {
            PlayerStats attackerStats = statsManager.getStats(attacker);
            isCustomCrit = (Math.random() * 100 < attackerStats.getCritChance());

            // 1a: Calculate base outgoing damage using the DamageManager
            finalDamage = damageManager.calculatePlayerDamage(attacker, victim, finalDamage, isCustomCrit);

            // 1b: Apply damage-modifying enchantments from the weapon
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (weapon != null && !weapon.getType().isAir()) {
                Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                    if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                    EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
                    if (effect != null) {
                        finalDamage = effect.onDamageModify(finalDamage, event, enchantment, entry.getValue(), isCustomCrit);
                    }
                }
            }
        }

        // --- Phase 2: Victim Logic ---
        finalDamage = damageManager.applyDefense(victim, finalDamage);

        // --- Finalization ---
        event.getEntity().setMetadata("last_hit_crit", new FixedMetadataValue(plugin, isCustomCrit));

        event.setDamage(Math.floor(finalDamage));;

        // --- Phase 3: Post-Hit Triggers ---
        // (This happens after the final damage value has been set)

        // 3a: Trigger Attacker's onAttack effects
        if (event.getDamager() instanceof Player attacker) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (weapon != null && !weapon.getType().isAir()) {
                Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                    if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                    EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
                    if (effect != null) {
                        effect.onAttack(event, enchantment, entry.getValue(), isCustomCrit);
                    }
                }
            }
            combatTracker.recordHit(attacker, victim);
        }

        // 3b: Trigger Victim's onDamaged effects (if victim is a Player)
        if (victim instanceof Player victimPlayer) {
            for (ItemStack armorPiece : victimPlayer.getInventory().getArmorContents()) {
                if (armorPiece == null || armorPiece.getType().isAir()) continue;
                Map<String, Integer> enchantments = enchantmentManager.getEnchantments(armorPiece);
                for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                    CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
                    if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
                    EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
                    if (effect != null) {
                        effect.onDamaged(event, enchantment, entry.getValue());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir()) return;

        Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
        if (enchantments.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            CustomEnchantment enchantment = enchantmentManager.getEnchantment(entry.getKey());
            if (enchantment == null || enchantment.getCustomLogicKey() == null) continue;
            EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
            if (effect != null) {
                effect.onKill(event, killer, enchantment, entry.getValue());
            }
        }
    }
}