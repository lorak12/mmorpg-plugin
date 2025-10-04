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

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Map;

public class DamageManager {

    private final MMORPGCore plugin;

    private final Gson gson = new Gson();
    private final Type statMapType = new TypeToken<Map<String, Double>>(){}.getType();
    public static final String BYPASS_DEFENSE_META_KEY = "mmorpg_bypass_defense";

    public DamageManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * --- NEW METHOD ---
     * Calculates the full outgoing damage of an attack, including stats and offensive enchantments.
     * @return The damage value before the victim's defense is applied.
     */
    public double calculatePlayerDamage(Player attacker, LivingEntity victim, double baseDamage, boolean isCritical) {
        PlayerStats attackerStats = plugin.getStatsManager().getStats(attacker);
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        double weaponDmg = 0; // Default to 0 for unarmed

        // --- THE FIX: Read DAMAGE stat directly from weapon NBT ---
        if (weapon != null && weapon.hasItemMeta()) {
            var data = weapon.getItemMeta().getPersistentDataContainer();
            if (data.has(ItemManager.BASE_STATS_KEY, PersistentDataType.STRING)) {
                String statsJson = data.get(ItemManager.BASE_STATS_KEY, PersistentDataType.STRING);
                Map<String, Double> itemStats = gson.fromJson(statsJson, statMapType);
                weaponDmg = itemStats.getOrDefault("DAMAGE", 0.0);
            }
        }

        // --- Step 1: Initial Damage ---
        double initialDamage = (5 + weaponDmg) * (1 + attackerStats.getStrength() / 100.0);

        // --- Step 2: Damage Multiplier ---
        double combatLevelBonus = 0;
        double enchantMultiplier = 0;
        double weaponBonus = 0;

        if (weapon != null) {
            Map<String, Integer> enchantments = plugin.getEnchantmentManager().getEnchantments(weapon);
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                CustomEnchantment enchant = plugin.getEnchantmentManager().getEnchantment(entry.getKey());
                if (enchant == null) continue;
                int level = entry.getValue();

                // Handle additive multipliers like Sharpness
                if (enchant.getId().equalsIgnoreCase("sharpness")) {
                    enchantMultiplier += enchant.getValue(level) / 100.0;
                }
                // Handle Giant Killer specifically
                if (enchant.getId().equalsIgnoreCase("giant_killer")) {
                    double playerHP = attacker.getHealth();
                    double enemyHP = victim.getHealth();
                    if (enemyHP > playerHP) {
                        double dmgPerPercentage = enchant.getValue(level);
                        double cap = enchant.getCost(level) / 100.0;
                        double bonus = Math.min(cap, ((enemyHP - playerHP) / playerHP) * dmgPerPercentage);
                        enchantMultiplier += bonus;
                    }
                }
                // Add other specific enchantment calculation logic here...
            }
        }

        double damageMultiplier = 1 + combatLevelBonus + enchantMultiplier + weaponBonus;
        double finalDamage = initialDamage * damageMultiplier;

        // --- Step 3: Critical Hit ---
        if (isCritical) {
            finalDamage *= (1 + attackerStats.getCritDamage() / 100.0);
        }

        return finalDamage;
    }

    /**
     * --- NEW METHOD ---
     * Applies the victim's defense calculations to an incoming damage value.
     * @return The final damage after defense reduction.
     */
    public double applyDefense(LivingEntity victim, double incomingDamage) {
        if (victim instanceof Player victimPlayer) {
            PlayerStats victimStats = plugin.getStatsManager().getStats(victimPlayer);
            double defense = victimStats.getDefense();
            double damageReduction = defense / (defense + 100.0);
            return incomingDamage * (1.0 - damageReduction);
        }

        // If the victim is not a player, they have no custom defense.
        // In the future, you could add a check here for custom mob defense stats.
        return incomingDamage;
    }

    /**
     * Initiates a damage event that scales like an ability and bypasses enemy defense.
     * It sets a metadata flag that PlayerDamageListener will check.
     * @param victim The entity to damage.
     * @param baseAbilityDamage The base damage of the ability before scaling.
     * @param attacker The player using the ability.
     */
    public void dealAbilityDamage(LivingEntity victim, double baseAbilityDamage, Player attacker) {
        PlayerStats stats = plugin.getStatsManager().getStats(attacker);

        // --- 1. Calculate the final damage here, inside the manager ---
        // Scale with the Ability Damage stat
        double finalDamage = baseAbilityDamage * (1 + (stats.getStat(Stat.ABILITY_DAMAGE) / 100.0));

        // Apply crit chance and crit damage
        boolean isCrit = (Math.random() * 100 < stats.getCritChance());
        if (isCrit) {
            finalDamage *= (1 + stats.getCritDamage() / 100.0);
            // You might want to play a crit sound or show a crit particle here or in the listener
        }

        // --- 2. Set the metadata flag ---
        victim.setMetadata(BYPASS_DEFENSE_META_KEY, new FixedMetadataValue(plugin, true));

        // --- 3. Call the damage event with the final calculated damage ---
        victim.damage(finalDamage, attacker);

        // The listener will see the metadata, skip the defense step, and then remove the metadata.
    }
}