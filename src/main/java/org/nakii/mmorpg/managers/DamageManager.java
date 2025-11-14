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

    public double calculatePlayerDamage(Player attacker, LivingEntity victim, double baseDamage, boolean isCritical) {
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

        double initialDamage = (5 + weaponDmg) * (1 + attackerStats.getStrength() / 100.0);

        double enchantMultiplier = 0;
        if (weapon != null) {
            Map<String, Integer> enchantments = enchantmentManager.getEnchantments(weapon);
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                CustomEnchantment enchant = enchantmentManager.getEnchantment(entry.getKey());
                if (enchant == null) continue;
                int level = entry.getValue();

                if ("sharpness".equalsIgnoreCase(enchant.getId())) {
                    enchantMultiplier += enchant.getValue(level) / 100.0;
                }
            }
        }

        double damageMultiplier = 1 + enchantMultiplier;
        double finalDamage = initialDamage * damageMultiplier;

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