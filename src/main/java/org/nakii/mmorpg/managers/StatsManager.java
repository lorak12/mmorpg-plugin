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
import org.nakii.mmorpg.util.Keys;

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
    private final DatabaseManager databaseManager;
    private final ItemManager itemManager;
    private final EnchantmentManager enchantmentManager;

    // These are no longer final and will be injected via setters after construction.
    private SkillManager skillManager;
    private EnchantmentEffectManager enchantmentEffectManager;
    private TimedBuffManager timedBuffManager;

    // Constructor is simplified to remove circular dependencies.
    public StatsManager(MMORPGCore plugin, DatabaseManager databaseManager, ItemManager itemManager, EnchantmentManager enchantmentManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.itemManager = itemManager;
        this.enchantmentManager = enchantmentManager;
    }

    // --- SETTER INJECTION METHODS ---
    public void setSkillManager(SkillManager skillManager) { this.skillManager = skillManager; }
    public void setEnchantmentEffectManager(EnchantmentEffectManager enchantmentEffectManager) { this.enchantmentEffectManager = enchantmentEffectManager; }
    public void setTimedBuffManager(TimedBuffManager timedBuffManager) { this.timedBuffManager = timedBuffManager; }


    public PlayerStats getStats(Player player) {
        return playerStatsMap.getOrDefault(player.getUniqueId(), new PlayerStats());
    }

    public void recalculateStats(Player player) {
        PlayerStats finalStats = new PlayerStats();
        applyBaseStats(finalStats);

        // Apply Skill Bonuses (check for null in case of reload/initialization race condition)
        if (skillManager != null) {
            for (Skill skill : Skill.values()) {
                int level = skillManager.getLevel(player, skill);
                if (level > 0) {
                    // Read from the fast in-memory cache instead of the file
                    Map<Stat, Double> rewards = skillManager.getCachedSkillRewards(skill);
                    for (Map.Entry<Stat, Double> entry : rewards.entrySet()) {
                        finalStats.addStat(entry.getKey(), level * entry.getValue());
                    }
                }
            }
        }

        Map<String, Integer> wornSetPieces = new HashMap<>();
        ItemStack[] equippedArmor = {
                player.getInventory().getHelmet(), player.getInventory().getChestplate(),
                player.getInventory().getLeggings(), player.getInventory().getBoots()
        };
        for (ItemStack armorPiece : equippedArmor) {
            if (armorPiece == null || !armorPiece.hasItemMeta()) continue;
            applyItemStats(armorPiece, finalStats);
            String setId = armorPiece.getItemMeta().getPersistentDataContainer().get(Keys.ARMOR_SET_ID, PersistentDataType.STRING);
            if (setId != null) {
                wornSetPieces.put(setId, wornSetPieces.getOrDefault(setId, 0) + 1);
            }
        }

        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem != null && !mainHandItem.getType().isAir()) {
            String typeName = mainHandItem.getType().name();
            if (!typeName.endsWith("_HELMET") && !typeName.endsWith("_CHESTPLATE") && !typeName.endsWith("_LEGGINGS") && !typeName.endsWith("_BOOTS")) {
                applyItemStats(mainHandItem, finalStats);
            }
        }

        for (ItemStack armorPiece : equippedArmor) {
            if (armorPiece == null || !armorPiece.hasItemMeta()) continue;
            var data = armorPiece.getItemMeta().getPersistentDataContainer();
            String setId = data.get(Keys.ARMOR_SET_ID, PersistentDataType.STRING);
            if (setId != null && wornSetPieces.getOrDefault(setId, 0) >= 4) {
                if (data.has(Keys.ARMOR_SET_STATS, PersistentDataType.STRING)) {
                    String statsJson = data.get(Keys.ARMOR_SET_STATS, PersistentDataType.STRING);
                    Type type = new TypeToken<Map<String, Double>>(){}.getType();
                    Map<String, Double> setBonusStats = gson.fromJson(statsJson, type);
                    setBonusStats.forEach((statName, value) -> finalStats.addStat(Stat.valueOf(statName), value));
                }
            }
        }

        // Apply Temporary Buffs (check for null)
        if (timedBuffManager != null) {
            for (Stat stat : Stat.values()) {
                finalStats.addStat(stat, timedBuffManager.getBuffsForStat(player, stat));
            }
        }

        PlayerBonusStats bonusStats = bonusStatsCache.get(player.getUniqueId());
        if (bonusStats != null) {
            bonusStats.getBonusStatsMap().forEach(finalStats::addStat);
        }

        playerStatsMap.put(player.getUniqueId(), finalStats);
        applyStatsToPlayer(player, finalStats);
    }

    private void applyItemStats(ItemStack item, PlayerStats stats) {
        if (item == null || !item.hasItemMeta()) return;

        var data = item.getItemMeta().getPersistentDataContainer();
        Gson gson = new Gson();
        Type statMapType = new TypeToken<Map<String, Double>>(){}.getType();

        if (data.has(Keys.BASE_STATS, PersistentDataType.STRING)) {
            String statsJson = data.get(Keys.BASE_STATS, PersistentDataType.STRING);
            Map<String, Double> baseStats = gson.fromJson(statsJson, statMapType);
            baseStats.forEach((statName, value) -> stats.addStat(Stat.valueOf(statName), value));
        }

        if (data.has(Keys.REFORGE_STATS, PersistentDataType.STRING)) {
            String statsJson = data.get(Keys.REFORGE_STATS, PersistentDataType.STRING);
            Map<String, Double> reforgeStats = gson.fromJson(statsJson, statMapType);
            reforgeStats.forEach((statName, value) -> stats.addStat(Stat.valueOf(statName), value));
        }

        Map<String, Integer> enchantments = enchantmentManager.getEnchantments(item);
        for (Map.Entry<String, Integer> enchantEntry : enchantments.entrySet()) {
            CustomEnchantment enchantment = enchantmentManager.getEnchantment(enchantEntry.getKey());
            if (enchantment == null) continue;
            int level = enchantEntry.getValue();

            enchantment.getStatModifiers().forEach((statName, formula) -> {
                try {
                    Stat stat = Stat.valueOf(statName);
                    Expression expression = new ExpressionBuilder(formula).variables("level", "value").build()
                            .setVariable("level", level).setVariable("value", enchantment.getValue(level));
                    stats.addStat(stat, expression.evaluate());
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to evaluate stat modifier for " + enchantment.getId() + ": " + e.getMessage());
                }
            });

            // Apply passive stats from enchantments (check for null)
            if (enchantment.getCustomLogicKey() != null && enchantmentEffectManager != null) {
                EnchantmentEffect effect = enchantmentEffectManager.getEffect(enchantment.getCustomLogicKey());
                if (effect != null) {
                    effect.onStatRecalculate(stats, enchantment, level);
                }
            }
        }
    }

    private void applyBaseStats(PlayerStats stats) {
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
        stats.setStat(Stat.MINING_SPEED, 0);
        stats.setStat(Stat.BREAKING_POWER, 0);
        stats.setStat(Stat.FORAGING_FORTUNE, 0);
        stats.setStat(Stat.FARMING_FORTUNE, 0);
        stats.setStat(Stat.MINING_FORTUNE, 0);
        stats.setStat(Stat.ORE_FORTUNE, 0);
        stats.setStat(Stat.BLOCK_FORTUNE, 0);
        stats.setStat(Stat.COMBAT_WISDOM, 0);
        stats.setStat(Stat.MINING_WISDOM, 0);
        stats.setStat(Stat.FARMING_WISDOM, 0);
        stats.setStat(Stat.FORAGING_WISDOM, 0);
        stats.setStat(Stat.FISHING_WISDOM, 0);
        stats.setStat(Stat.ENCHANTING_WISDOM, 0);
        stats.setStat(Stat.ALCHEMY_WISDOM, 0);
        stats.setStat(Stat.CARPENTRY_WISDOM, 0);
        stats.setStat(Stat.SPEED, 100);
        stats.setStat(Stat.MAGIC_FIND, 0);
        stats.setStat(Stat.COLD_RESISTANCE, 0);
        stats.setStat(Stat.HEAT_RESISTANCE, 0);
        stats.setStat(Stat.SEA_CREATURE_CHANCE, 20);
        stats.setStat(Stat.FISHING_SPEED, 0);
        stats.setStat(Stat.TREASURE_CHANCE, 0);
    }

    /**
     * Bridges our custom stat system with Bukkit's Attribute system.
     * This is where we control the visual display of hearts and speed.
     */
    private void applyStatsToPlayer(Player player, PlayerStats stats) {
        // --- HEALTH SCALING FIX ---
        // The player's VISUAL hearts are now capped at 40.0 (2 rows).
        // The actual MMORPG health stat (stats.getHealth()) can be thousands or millions.
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0);

        // Set Movement Speed (this logic is correct)
        double baseSpeed = 0.1 * (stats.getSpeed() / 100.0);
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(baseSpeed);
    }


    public void loadPlayer(Player player) {
        recalculateStats(player);
    }

    public void unloadPlayer(Player player) {
        playerStatsMap.remove(player.getUniqueId());
    }

    private final Map<UUID, PlayerBonusStats> bonusStatsCache = new ConcurrentHashMap<>();

    public void addPermanentBonus(Player player, Stat stat, double amount) {
        PlayerBonusStats bonusStats = bonusStatsCache.get(player.getUniqueId());
        if (bonusStats != null) {
            bonusStats.addBonus(stat, amount);
            recalculateStats(player);
        }
    }

    public void loadPlayerData(Player player) {
        try {
            bonusStatsCache.put(player.getUniqueId(), databaseManager.loadPlayerBonusStats(player.getUniqueId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unloadPlayerData(Player player) {
        PlayerBonusStats data = bonusStatsCache.remove(player.getUniqueId());
        if (data != null) {
            try {
                databaseManager.savePlayerBonusStats(player.getUniqueId(), data);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}