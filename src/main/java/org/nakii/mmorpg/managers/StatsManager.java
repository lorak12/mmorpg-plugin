package org.nakii.mmorpg.managers;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.enchantment.effects.EnchantmentEffect;
import org.nakii.mmorpg.player.PlayerBonusStats;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.skills.Skill;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {

    private final MMORPGCore plugin;
    private final Map<UUID, PlayerStats> playerStatsMap = new HashMap<>();
    private final Gson gson = new Gson();

    public StatsManager(MMORPGCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Retrieves the cached final stats for a player.
     * @param player The player whose stats to retrieve.
     * @return The PlayerStats object, or a new empty one if not found.
     */
    public PlayerStats getStats(Player player) {
        return playerStatsMap.getOrDefault(player.getUniqueId(), new PlayerStats());
    }

    /**
     * The core stat calculation engine. Gathers stats from all sources,
     * calculates the final values, and applies them to the player.
     * This should be called whenever a player's gear, skills, or buffs change.
     *
     * @param player The player to recalculate stats for.
     */
    public void recalculateStats(Player player) {
        PlayerStats finalStats = new PlayerStats();

        // Layer 1a: Base & Permanent Stats
        applyBaseStats(finalStats);

        // --- START OF NEW/MODIFIED LOGIC ---
        // 1b. Apply Skill Bonuses
        FileConfiguration skillsConfig = plugin.getSkillManager().getSkillsConfig();
        for (Skill skill : Skill.values()) {
            int level = plugin.getSkillManager().getLevel(player, skill);
            if (level > 0) {
                ConfigurationSection rewards = skillsConfig.getConfigurationSection(skill.name() + ".rewards-per-level");
                if (rewards != null) {
                    for (String statKey : rewards.getKeys(false)) {
                        try {
                            Stat stat = Stat.valueOf(statKey.toUpperCase());
                            double amountPerLevel = rewards.getDouble(statKey);
                            finalStats.addStat(stat, level * amountPerLevel);
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        }
        // --- END OF NEW/MODIFIED LOGIC ---

        // Layer 2: Equipment Stats
        Map<String, Integer> wornSetPieces = new HashMap<>();

        // --- 2a: Process EQUIPPED ARMOR ONLY ---
        ItemStack[] equippedArmor = {
                player.getInventory().getHelmet(), player.getInventory().getChestplate(),
                player.getInventory().getLeggings(), player.getInventory().getBoots()
        };
        for (ItemStack armorPiece : equippedArmor) {
            if (armorPiece == null || !armorPiece.hasItemMeta()) continue;

            applyItemStats(armorPiece, finalStats); // Apply its stats

            // Count pieces for set bonus
            String setId = armorPiece.getItemMeta().getPersistentDataContainer().get(ItemManager.ARMOR_SET_ID_KEY, PersistentDataType.STRING);
            if (setId != null) {
                wornSetPieces.put(setId, wornSetPieces.getOrDefault(setId, 0) + 1);
            }
        }

        // --- 2b: Process HELD ITEM (if it's not armor) ---
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem != null && !mainHandItem.getType().isAir()) {
            String typeName = mainHandItem.getType().name();
            // This check ensures we don't add stats from armor being held in the hand.
            if (!typeName.endsWith("_HELMET") && !typeName.endsWith("_CHESTPLATE") && !typeName.endsWith("_LEGGINGS") && !typeName.endsWith("_BOOTS")) {
                applyItemStats(mainHandItem, finalStats);
            }
        }

        // --- 2c: Apply Full Set Bonuses ---
        // This check is now separate and runs after all pieces have been counted.
        for (ItemStack armorPiece : equippedArmor) {
            if (armorPiece == null || !armorPiece.hasItemMeta()) continue;
            var data = armorPiece.getItemMeta().getPersistentDataContainer();
            String setId = data.get(ItemManager.ARMOR_SET_ID_KEY, PersistentDataType.STRING);

            if (setId != null && wornSetPieces.getOrDefault(setId, 0) >= 4) { // Assuming 4 pieces
                if (data.has(ItemManager.ARMOR_SET_STATS_KEY, PersistentDataType.STRING)) {
                    String statsJson = data.get(ItemManager.ARMOR_SET_STATS_KEY, PersistentDataType.STRING);
                    Type type = new TypeToken<Map<String, Double>>(){}.getType();
                    Map<String, Double> setBonusStats = gson.fromJson(statsJson, type);
                    setBonusStats.forEach((statName, value) -> finalStats.addStat(Stat.valueOf(statName), value));
                }
            }
        }

        // --- Layer 3: Temporary Buffs ---
        for (Stat stat : Stat.values()) {
            finalStats.addStat(stat, plugin.getTimedBuffManager().getBuffsForStat(player, stat));
        }

        // 4. Apply Permanent Stat Bonuses (from Collections, Slayers, etc.)
        PlayerBonusStats bonusStats = bonusStatsCache.get(player.getUniqueId());
        if (bonusStats != null) {
            bonusStats.getBonusStatsMap().forEach(finalStats::addStat);
        }

        // --- Finalization ---
        playerStatsMap.put(player.getUniqueId(), finalStats);
        applyStatsToPlayer(player, finalStats);
    }

    /**
     * --- NEW HELPER METHOD ---
     * A private helper method to apply all stat types (base, reforge, enchant)
     * from a single ItemStack to a PlayerStats object.
     * This avoids code duplication.
     */
    private void applyItemStats(ItemStack item, PlayerStats stats) {
        if (item == null || !item.hasItemMeta()) return;

        var data = item.getItemMeta().getPersistentDataContainer();
        Gson gson = new Gson();
        Type statMapType = new TypeToken<Map<String, Double>>(){}.getType();

        // Base stats
        if (data.has(ItemManager.BASE_STATS_KEY, PersistentDataType.STRING)) {
            String statsJson = data.get(ItemManager.BASE_STATS_KEY, PersistentDataType.STRING);
            Map<String, Double> baseStats = gson.fromJson(statsJson, statMapType);
            baseStats.forEach((statName, value) -> stats.addStat(Stat.valueOf(statName), value));
        }

        // Reforge stats
        if (data.has(ItemManager.REFORGE_STATS_KEY, PersistentDataType.STRING)) {
            String statsJson = data.get(ItemManager.REFORGE_STATS_KEY, PersistentDataType.STRING);
            Map<String, Double> reforgeStats = gson.fromJson(statsJson, statMapType);
            reforgeStats.forEach((statName, value) -> stats.addStat(Stat.valueOf(statName), value));
        }

        // Enchantment stats
        Map<String, Integer> enchantments = plugin.getEnchantmentManager().getEnchantments(item);
        for (Map.Entry<String, Integer> enchantEntry : enchantments.entrySet()) {
            CustomEnchantment enchantment = plugin.getEnchantmentManager().getEnchantment(enchantEntry.getKey());
            if (enchantment == null) continue;
            int level = enchantEntry.getValue();

            enchantment.getStatModifiers().forEach((statName, formula) -> {
                try {
                    Stat stat = Stat.valueOf(statName);
                    Expression expression = new ExpressionBuilder(formula).variables("level", "value").build()
                            .setVariable("level", level).setVariable("value", enchantment.getValue(level));
                    stats.addStat(stat, expression.evaluate());
                } catch (Exception e) { /* Log error */ }
            });

            if (enchantment.getCustomLogicKey() != null) {
                EnchantmentEffect effect = plugin.getEnchantmentEffectManager().getEffect(enchantment.getCustomLogicKey());
                if (effect != null) {
                    effect.onStatRecalculate(stats, enchantment, level);
                }
            }
        }
    }

    /**
     * Sets the default base stats for a player, as defined in the project documentation.
     * This is the starting point before any skills, items, or buffs are applied.
     */
    private void applyBaseStats(PlayerStats stats) {
        // --- Combat Stats ---
        stats.setStat(Stat.HEALTH, 100);
        stats.setStat(Stat.DEFENSE, 0);
        stats.setStat(Stat.STRENGTH, 0);
        stats.setStat(Stat.INTELLIGENCE, 100);
        stats.setStat(Stat.CRIT_CHANCE, 30);
        stats.setStat(Stat.CRIT_DAMAGE, 50);
        stats.setStat(Stat.BONUS_ATTACK_SPEED, 0);
        stats.setStat(Stat.ABILITY_DAMAGE, 0);
        stats.setStat(Stat.TRUE_DEFENSE, 0);
        stats.setStat(Stat.FEROCITY, 0);
        stats.setStat(Stat.HEALTH_REGEN, 100);
        stats.setStat(Stat.VITALITY, 100);
        stats.setStat(Stat.SWING_RANGE, 3);

        // --- Gathering Stats ---
        stats.setStat(Stat.MINING_SPEED, 0);
        stats.setStat(Stat.BREAKING_POWER, 0);
        stats.setStat(Stat.FORAGING_FORTUNE, 0);
        stats.setStat(Stat.FARMING_FORTUNE, 0);
        stats.setStat(Stat.MINING_FORTUNE, 0);
        stats.setStat(Stat.ORE_FORTUNE, 0);
        stats.setStat(Stat.BLOCK_FORTUNE, 0);

        // --- Wisdom Stats ---
        stats.setStat(Stat.COMBAT_WISDOM, 0);
        stats.setStat(Stat.MINING_WISDOM, 0);
        stats.setStat(Stat.FARMING_WISDOM, 0);
        stats.setStat(Stat.FORAGING_WISDOM, 0);
        stats.setStat(Stat.FISHING_WISDOM, 0);
        stats.setStat(Stat.ENCHANTING_WISDOM, 0);
        stats.setStat(Stat.ALCHEMY_WISDOM, 0);
        stats.setStat(Stat.CARPENTRY_WISDOM, 0);

        // --- Misc Stats ---
        stats.setStat(Stat.SPEED, 100); // Represented as a percentage
        stats.setStat(Stat.MAGIC_FIND, 0);
        stats.setStat(Stat.COLD_RESISTANCE, 0);
        stats.setStat(Stat.HEAT_RESISTANCE, 0);

        // --- Fishing Stats ---
        stats.setStat(Stat.SEA_CREATURE_CHANCE, 20); // Represented as a percentage
        stats.setStat(Stat.FISHING_SPEED, 0);
        stats.setStat(Stat.TREASURE_CHANCE, 0);

        // --- Special Stats (Not in Stat enum, handled separately) ---
        // MANA: Base value is derived from Intelligence.
        // ABSORPTION: Handled by Bukkit effects.
        // EFFECTIVE_HEALTH: A calculated value, not a base stat.
    }

    /**
     * Applies permanent stat bonuses from a player's skill levels.
     * (This is a placeholder for your SkillManager's logic)
     */
    private void applySkillBonuses(Player player, PlayerStats stats) {
        // Example logic:
        // int farmingLevel = plugin.getSkillManager().getLevel(player, Skill.FARMING);
        // stats.addStat(Stat.HEALTH, farmingLevel * 2); // +2 Health per Farming level
        //
        // int combatLevel = plugin.getSkillManager().getLevel(player, Skill.COMBAT);
        // stats.addStat(Stat.CRIT_CHANCE, combatLevel * 0.5); // +0.5% Crit Chance per Combat level
    }

    /**
     * Bridges our custom stat system with Bukkit's Attribute system
     * for core mechanics like health and speed.
     */
    private void applyStatsToPlayer(Player player, PlayerStats stats) {
        // Set Max Health
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(stats.getHealth());

        // Set Movement Speed
        // Vanilla base speed is 0.1. Our 100 Speed = 0.1. 1 Speed = 0.001.
        double baseSpeed = 0.1 * (stats.getSpeed() / 100.0);
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(baseSpeed);

        // Note: Defense, Strength, etc., are custom. They are not applied to Bukkit attributes.
        // Their effects are calculated manually in our PlayerDamageListener.
    }

    // --- Player Session Management ---
    public void loadPlayer(Player player) {
        // In a real system, you would load saved stats from a database here.
        // For now, we just calculate them from scratch.
        recalculateStats(player);
    }

    public void unloadPlayer(Player player) {
        playerStatsMap.remove(player.getUniqueId());
    }

    private final Map<UUID, PlayerBonusStats> bonusStatsCache = new ConcurrentHashMap<>();

    // --- ADD THIS NEW PUBLIC METHOD ---
    public void addPermanentBonus(Player player, Stat stat, double amount) {
        PlayerBonusStats bonusStats = bonusStatsCache.get(player.getUniqueId());
        if (bonusStats != null) {
            bonusStats.addBonus(stat, amount);
            // Recalculate stats immediately to apply the change
            recalculateStats(player);
        }
    }

    // --- ADD THESE LOAD/UNLOAD METHODS ---
    public void loadPlayerData(Player player) {
        try {
            bonusStatsCache.put(player.getUniqueId(), plugin.getDatabaseManager().loadPlayerBonusStats(player.getUniqueId()));
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error, maybe kick player
        }
    }

    public void unloadPlayerData(Player player) {
        PlayerBonusStats data = bonusStatsCache.remove(player.getUniqueId());
        if (data != null) {
            try {
                plugin.getDatabaseManager().savePlayerBonusStats(player.getUniqueId(), data);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}