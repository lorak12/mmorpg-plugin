package org.nakii.mmorpg.managers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.util.Keys;

import java.lang.reflect.Type;
import java.util.Map;

public class DamageManager {

    private final MMORPGCore plugin;
    private final StatsManager statsManager;
    private final EnchantmentManager enchantmentManager;
    private final Gson gson = new Gson();
    private final Type statMapType = new TypeToken<Map<String, Double>>() {}.getType();

    public DamageManager(MMORPGCore plugin, StatsManager statsManager, EnchantmentManager enchantmentManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.enchantmentManager = enchantmentManager;
    }

    /**
     * The new, centralized method for calculating all player-dealt damage from a standard attack.
     * @param attacker The attacking player.
     * @param victim The entity being attacked.
     * @param baseDamage The initial damage from the event (used as a fallback).
     * @param isCritical Whether the hit is a custom critical hit.
     * @param isBackstab Whether the hit is a backstab (for effects like Livid Dagger).
     * @return The final calculated damage before defense and on-damage-modify enchantments are applied.
     */
    public double calculateFinalPlayerDamage(Player attacker, LivingEntity victim, double baseDamage, boolean isCritical, boolean isBackstab) {
        // Step 1: Calculate base damage from stats and weapon.
        double finalDamage = calculatePlayerBaseDamage(attacker, isCritical);

        // Step 2: Apply conditional multipliers from item effects.
        if (isBackstab && isCritical) {
            // Livid Dagger's special logic now lives here, where it belongs.
            finalDamage *= 2.0;
        }

        // Add other conditional multipliers here in the future (e.g., from armor sets).
        // Example: if (player is wearing full Dragon Armor and victim is a Dragon) { finalDamage *= 1.5; }

        return finalDamage;
    }

    /**
     * Calculates the initial damage based on player stats, weapon, and critical hits.
     * This is a private helper method for the main damage calculation.
     */
    private double calculatePlayerBaseDamage(Player attacker, boolean isCritical) {
        PlayerStats attackerStats = statsManager.getStats(attacker);
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        double weaponDmg = 0;

        if (weapon != null && weapon.hasItemMeta()) {
            var data = weapon.getItemMeta().getPersistentDataContainer();
            if (data.has(Keys.BASE_STATS, PersistentDataType.STRING)) {
                String statsJson = data.get(Keys.BASE_STATS, PersistentDataType.STRING);
                Map<String, Double> itemStats = gson.fromJson(statsJson, statMapType);
                weaponDmg = itemStats.getOrDefault("DAMAGE", 0.0);
            }
        }

        // The core damage formula
        double initialDamage = (5 + weaponDmg) * (1 + attackerStats.getStrength() / 100.0);

        // Apply sharpness-like enchantments
        double enchantMultiplier = 0;
        if (weapon != null) {
            Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
            Integer sharpnessLevel = enchantments.get("sharpness");
            if (sharpnessLevel != null) {
                CustomEnchantment enchant = enchantmentManager.getEnchantment("sharpness");
                if (enchant != null) {
                    enchantMultiplier += enchant.getValue(sharpnessLevel) / 100.0;
                }
            }
        }

        double damageMultiplier = 1 + enchantMultiplier;
        double finalDamage = initialDamage * damageMultiplier;

        // Apply critical hit multiplier
        if (isCritical) {
            finalDamage *= (1 + attackerStats.getCritDamage() / 100.0);
        }

        return finalDamage;
    }

    public double applyDefense(LivingEntity victim, double incomingDamage) {
        if (victim instanceof Player victimPlayer) {
            PlayerStats victimStats = statsManager.getStats(victimPlayer);
            double defense = victimStats.getDefense();
            double damageReduction = defense / (defense + 100.0);
            return incomingDamage * (1.0 - damageReduction);
        }
        // Could add logic for custom mob defense here later
        return incomingDamage;
    }

    public void dealAbilityDamage(LivingEntity victim, double baseAbilityDamage, Player attacker) {
        PlayerStats stats = statsManager.getStats(attacker);
        double finalDamage = baseAbilityDamage * (1 + (stats.getStat(Stat.ABILITY_DAMAGE) / 100.0));

        boolean isCrit = (Math.random() * 100 < stats.getCritChance());
        if (isCrit) {
            finalDamage *= (1 + stats.getCritDamage() / 100.0);
        }

        victim.setMetadata(Keys.BYPASS_DEFENSE.getKey(), new FixedMetadataValue(plugin, true));
        victim.damage(finalDamage, attacker);
    }
}