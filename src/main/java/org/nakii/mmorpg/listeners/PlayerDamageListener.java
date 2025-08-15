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
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;

import java.util.List;
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

        // Neutralize vanilla critical damage
        if (event.isCritical()) {
            finalDamage /= 1.5;
        }

        // --- Phase 1: Determine Attacker's Base Damage ---

        // 1a: If the attacker is a PLAYER
        if (event.getDamager() instanceof Player attacker) {
            PlayerStats attackerStats = statsManager.getStats(attacker);
            isCustomCrit = (Math.random() * 100 < attackerStats.getCritChance());

            // Calculate full outgoing damage, including stats and offensive enchants
            finalDamage = damageManager.calculatePlayerDamage(attacker, victim, finalDamage, isCustomCrit);

            // Apply damage-modifying enchantments
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
        // 1b: --- THIS IS THE NEW LOGIC BLOCK ---
        // If the attacker is a MOB
        else if (event.getDamager() instanceof LivingEntity mobAttacker) {
            // Check if it's one of our custom mobs
            if (plugin.getMobManager().isCustomMob(mobAttacker)) {
                CustomMobTemplate template = plugin.getMobManager().getTemplate(plugin.getMobManager().getMobId(mobAttacker));
                if (template != null) {
                    // Set the event's base damage to our custom mob's damage stat.
                    // In the future, you could add a formula here for mob Strength, etc.
                    finalDamage = template.getStat(Stat.DAMAGE);
                }
            }
        }

        // --- Phase 2: Victim Logic (Apply Defenses) ---
        finalDamage = damageManager.applyDefense(victim, finalDamage);

        // We attach a temporary piece of metadata to the entity that was hit.
        // This metadata will be readable by our lower-priority listener.
        // The key is a unique string, and the value is the boolean result.
        event.getEntity().setMetadata("mmorpg_last_hit_crit", new FixedMetadataValue(plugin, isCustomCrit));

        // --- Finalization ---
        event.setDamage(Math.floor(finalDamage));

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
        if (killer == null) return; // Must be killed by a player

        LivingEntity victim = event.getEntity();
        String mobId = plugin.getMobManager().getMobId(victim);

        // --- 1. Handle Custom Loot ---
        if (mobId != null) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            List<ItemStack> customLoot = plugin.getLootManager().rollLootTable(killer, mobId);
            event.getDrops().addAll(customLoot);
        }

        // --- 2. THE FIX: Trigger Combat XP Gain ---
        // This call is the missing link. It will cause the SkillManager to
        // calculate combat XP and fire the PlayerGainCombatXpEvent,
        // which our SlayerProgressListener is listening for.
        plugin.getSkillManager().handleMobKill(killer, victim);

        // --- 3. Handle onKill Enchantment Effects ---
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