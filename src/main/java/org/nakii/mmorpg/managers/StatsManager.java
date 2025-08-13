package org.nakii.mmorpg.managers;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.enchantment.CustomEnchantment;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final MMORPGCore plugin;
    private final Map<UUID, PlayerStats> playerStatsMap = new HashMap<>();

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
        EnchantmentManager enchantmentManager = plugin.getEnchantmentManager();

        // --- Layer 1: Base & Permanent Stats ---
        applyBaseStats(finalStats);
        applySkillBonuses(player, finalStats);

        // --- Layer 2: Equipment Stats ---
        // We check all armor slots plus the item in the main hand.
        ItemStack[] itemsToCheck = {
                player.getInventory().getHelmet(),
                player.getInventory().getChestplate(),
                player.getInventory().getLeggings(),
                player.getInventory().getBoots(),
                player.getInventory().getItemInMainHand()
                // In the future, you can add Equipment slots (Gloves, etc.) here
        };

        for (ItemStack item : itemsToCheck) {
            if (item == null || item.getType().isAir()) continue;

            // 2a: Add base stats from the custom item itself (from ItemManager)
            // Map<Stat, Double> itemBaseStats = plugin.getItemManager().getBaseStats(item);
            // itemBaseStats.forEach(finalStats::addStat);

            // 2b: Add stats from the item's custom enchantments
            Map<String, Integer> enchantments = enchantmentManager.getEnchantments(item);
            for (Map.Entry<String, Integer> enchantEntry : enchantments.entrySet()) {
                CustomEnchantment enchantment = enchantmentManager.getEnchantment(enchantEntry.getKey());
                int level = enchantEntry.getValue();

                if (enchantment == null) continue;

                // Process stat-modifying enchantments (e.g., Growth, Protection)
                Map<String, String> modifiers = enchantment.getStatModifiers();
                for (Map.Entry<String, String> modEntry : modifiers.entrySet()) {
                    try {
                        Stat stat = Stat.valueOf(modEntry.getKey().toUpperCase());
                        String formula = modEntry.getValue();

                        // Evaluate the formula, replacing 'level' and 'value' with actual numbers
                        Expression expression = new ExpressionBuilder(formula)
                                .variables("level", "value")
                                .build()
                                .setVariable("level", level)
                                .setVariable("value", enchantment.getValue(level));

                        finalStats.addStat(stat, expression.evaluate());

                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid stat '" + modEntry.getKey() + "' in enchantment '" + enchantment.getId() + "'.");
                    } catch (Exception e) {
                        plugin.getLogger().warning("Could not evaluate formula for enchant '" + enchantment.getId() + "': " + e.getMessage());
                    }
                }
            }
            // 2c: In the future, you would add stats from Reforges here.
        }

        // --- Layer 3: Temporary Buffs ---
        // 3a: Add stats from the TimedBuffManager (e.g., Counter-Strike)
        for (Stat stat : Stat.values()) {
            finalStats.addStat(stat, plugin.getTimedBuffManager().getBuffsForStat(player, stat));
        }

        // 3b: In the future, you would add stats from Pets and Potions here.


        // --- Finalization ---
        // Cache the newly calculated stats
        playerStatsMap.put(player.getUniqueId(), finalStats);

        // Apply the relevant stats to the live Bukkit Player object
        applyStatsToPlayer(player, finalStats);
    }

    /**
     * Sets the default base stats for a player.
     */
    private void applyBaseStats(PlayerStats stats) {
        stats.setStat(Stat.HEALTH, 100);
        stats.setStat(Stat.DEFENSE, 0);
        stats.setStat(Stat.STRENGTH, 0);
        stats.setStat(Stat.INTELLIGENCE, 100);
        stats.setStat(Stat.CRIT_CHANCE, 30);
        stats.setStat(Stat.CRIT_DAMAGE, 50);
        stats.setStat(Stat.SPEED, 100);
        // ... set other base stats from your documentation
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
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(stats.getHealth());

        // Set Movement Speed
        // Vanilla base speed is 0.1. Our 100 Speed = 0.1. 1 Speed = 0.001.
        double baseSpeed = 0.1 * (stats.getSpeed() / 100.0);
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(baseSpeed);

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
}