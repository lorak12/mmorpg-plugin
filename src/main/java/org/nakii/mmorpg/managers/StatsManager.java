package org.nakii.mmorpg.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.stats.PlayerStats;
import org.nakii.mmorpg.stats.StatBreakdown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final MMORPGCore plugin;
    private final Map<UUID, PlayerStats> playerStatsMap;

    public StatsManager(MMORPGCore plugin) {
        this.plugin = plugin;
        this.playerStatsMap = new HashMap<>();
    }

    public void registerPlayer(Player player) {
        playerStatsMap.put(player.getUniqueId(), new PlayerStats());
        recalculateStats(player);
    }

    public void unregisterPlayer(Player player) {
        playerStatsMap.remove(player.getUniqueId());
    }

    public PlayerStats getStats(Player player) {
        return playerStatsMap.getOrDefault(player.getUniqueId(), new PlayerStats());
    }

    /**
     * Recalculates all stats for a player from base values, items, and skills.
     */
    public void recalculateStats(Player player) {
        PlayerStats finalStats = new PlayerStats(); // Starts with fresh base stats
        PlayerStats itemStats = new PlayerStats(true); // Empty stats for items
        PlayerStats skillStats = new PlayerStats(true); // Empty stats for skills

        // 1. Calculate stat bonuses from items
        PlayerInventory inventory = player.getInventory();
        addStatsFromItem(itemStats, inventory.getItemInMainHand());
        addStatsFromItem(itemStats, inventory.getItemInOffHand());
        for (ItemStack armor : inventory.getArmorContents()) {
            addStatsFromItem(itemStats, armor);
        }
        // TODO: Add stats from accessories in the future

        // 2. Calculate stat bonuses from skills
        calculateSkillStats(player, skillStats);

        // 3. Combine all stat sources
        // Combat
        finalStats.addHealth(itemStats.getHealth() + skillStats.getHealth());
        finalStats.addDefense(itemStats.getDefense() + skillStats.getDefense());
        finalStats.addStrength(itemStats.getStrength() + skillStats.getStrength());
        finalStats.addIntelligence(itemStats.getIntelligence() + skillStats.getIntelligence());
        finalStats.addCritChance(itemStats.getCritChance() + skillStats.getCritChance());
        finalStats.addCritDamage(itemStats.getCritDamage() + skillStats.getCritDamage());
        finalStats.addBonusAttackSpeed(itemStats.getBonusAttackSpeed() + skillStats.getBonusAttackSpeed());
        finalStats.addAbilityDamage(itemStats.getAbilityDamage() + skillStats.getAbilityDamage());
        finalStats.addTrueDefense(itemStats.getTrueDefense() + skillStats.getTrueDefense());
        finalStats.addFerocity(itemStats.getFerocity() + skillStats.getFerocity());
        finalStats.addHealthRegen(itemStats.getHealthRegen() + skillStats.getHealthRegen());
        finalStats.addVitality(itemStats.getVitality() + skillStats.getVitality());
        finalStats.addSwingRange(itemStats.getSwingRange() + skillStats.getSwingRange());
        // Gathering
        finalStats.addMiningSpeed(itemStats.getMiningSpeed() + skillStats.getMiningSpeed());
        finalStats.addFarmingFortune(itemStats.getFarmingFortune() + skillStats.getFarmingFortune());
        finalStats.addMiningFortune(itemStats.getMiningFortune() + skillStats.getMiningFortune());
        finalStats.addForagingFortune(itemStats.getForagingFortune() + skillStats.getForagingFortune());
        // Misc
        finalStats.addSpeed(itemStats.getSpeed() + skillStats.getSpeed());
        finalStats.addMagicFind(itemStats.getMagicFind() + skillStats.getMagicFind());
        // Internal
        finalStats.addDamage(itemStats.getDamage() + skillStats.getDamage());


        // 4. Put the final calculated stats into the map
        playerStatsMap.put(player.getUniqueId(), finalStats);

        // 5. Apply the results to the player entity
        applyVanillaAttributes(player, finalStats);
    }

    private void addStatsFromItem(PlayerStats stats, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(new NamespacedKey(plugin, "item_id"), PersistentDataType.STRING)) {
            return;
        }

        // This is a dynamic way to read all stats without a giant block of code.
        // It assumes your stat names in PlayerStats match the keys in your item files.
        stats.addHealth(getStatFromItem(container, "health"));
        stats.addDefense(getStatFromItem(container, "defense"));
        stats.addStrength(getStatFromItem(container, "strength"));
        stats.addIntelligence(getStatFromItem(container, "intelligence"));
        stats.addCritChance(getStatFromItem(container, "crit_chance"));
        stats.addCritDamage(getStatFromItem(container, "crit_damage"));
        stats.addBonusAttackSpeed(getStatFromItem(container, "bonus_attack_speed"));
        stats.addAbilityDamage(getStatFromItem(container, "ability_damage"));
        stats.addTrueDefense(getStatFromItem(container, "true_defense"));
        stats.addFerocity(getStatFromItem(container, "ferocity"));
        stats.addHealthRegen(getStatFromItem(container, "health_regen"));
        stats.addVitality(getStatFromItem(container, "vitality"));
        stats.addSwingRange(getStatFromItem(container, "swing_range"));
        stats.addMiningSpeed(getStatFromItem(container, "mining_speed"));
        stats.addBreakingPower(getStatFromItem(container, "breaking_power"));
        stats.addForagingFortune(getStatFromItem(container, "foraging_fortune"));
        stats.addFarmingFortune(getStatFromItem(container, "farming_fortune"));
        stats.addMiningFortune(getStatFromItem(container, "mining_fortune"));
        stats.addOreFortune(getStatFromItem(container, "ore_fortune"));
        stats.addBlockFortune(getStatFromItem(container, "block_fortune"));
        stats.addSpeed(getStatFromItem(container, "speed"));
        stats.addMagicFind(getStatFromItem(container, "magic_find"));
        stats.addColdResistance(getStatFromItem(container, "cold_resistance"));
        stats.addHeatResistance(getStatFromItem(container, "heat_resistance"));
        stats.addSeaCreatureChance(getStatFromItem(container, "sea_creature_chance"));
        stats.addFishingSpeed(getStatFromItem(container, "fishing_speed"));
        stats.addTreasureChance(getStatFromItem(container, "treasure_chance"));
        stats.addDamage(getStatFromItem(container, "damage"));
    }

    private double getStatFromItem(PersistentDataContainer container, String statName) {
        return container.getOrDefault(new NamespacedKey(plugin, "stat_" + statName), PersistentDataType.DOUBLE, 0.0);
    }

    private void calculateSkillStats(Player player, PlayerStats skillStats) {
        PlayerSkillData skillData = plugin.getSkillManager().getSkillData(player);
        if (skillData == null) return;

        // Rewards as per the design document
        skillStats.addDefense(skillData.getLevel(Skill.MINING) * plugin.getConfig().getDouble("skills.mining.rewards.defense_per_level", 0));
        skillStats.addStrength(skillData.getLevel(Skill.FORAGING) * plugin.getConfig().getDouble("skills.foraging.rewards.strength_per_level", 0));
        skillStats.addHealth(skillData.getLevel(Skill.FARMING) * plugin.getConfig().getDouble("skills.farming.rewards.health_per_level", 0));
        skillStats.addHealth(skillData.getLevel(Skill.FISHING) * plugin.getConfig().getDouble("skills.fishing.rewards.health_per_level", 0));
        skillStats.addHealth(skillData.getLevel(Skill.CARPENTRY) * plugin.getConfig().getDouble("skills.carpentry.rewards.health_per_level", 0));
        skillStats.addIntelligence(skillData.getLevel(Skill.ENCHANTING) * plugin.getConfig().getDouble("skills.enchanting.rewards.intelligence_per_level", 0));
        skillStats.addIntelligence(skillData.getLevel(Skill.ALCHEMY) * plugin.getConfig().getDouble("skills.alchemy.rewards.intelligence_per_level", 0));
        skillStats.addCritChance(skillData.getLevel(Skill.COMBAT) * plugin.getConfig().getDouble("skills.combat.rewards.crit_chance_per_level", 0));
    }

    private void applyVanillaAttributes(Player player, PlayerStats stats) {
        // This line is critical for the HUDManager to work correctly.
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(stats.getHealth());

        double minecraftSpeed = 0.1 * (stats.getSpeed() / 100.0);
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(minecraftSpeed);

        double baseAttackSpeed = 4.0;
        double maxBonus = 100.0;
        double maxAttackSpeed = 20.0;
        double finalAttackSpeed = baseAttackSpeed + ((maxAttackSpeed - baseAttackSpeed) * (stats.getBonusAttackSpeed() / maxBonus));
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(finalAttackSpeed);

        player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE).setBaseValue(stats.getSwingRange());
    }

    /**
     * Generates a detailed breakdown of a player's stats for GUI or command display.
     * @param player The player to get the breakdown for.
     * @return A structured StatBreakdown object.
     */
    public StatBreakdown getStatBreakdown(Player player) {
        // 1. Create containers for each stat source.
        PlayerStats baseStats = new PlayerStats(); // Contains all default values.
        PlayerStats itemStats = new PlayerStats(true); // Starts empty.
        PlayerStats skillStats = new PlayerStats(true); // Starts empty.

        // 2. Populate the itemStats object.
        PlayerInventory inventory = player.getInventory();
        addStatsFromItem(itemStats, inventory.getItemInMainHand());
        addStatsFromItem(itemStats, inventory.getItemInOffHand());
        for (ItemStack armor : inventory.getArmorContents()) {
            addStatsFromItem(itemStats, armor);
        }
        // TODO: Add accessories here in the future.

        // 3. Populate the skillStats object.
        calculateSkillStats(player, skillStats);

        // 4. Get the final total stats from our main map.
        PlayerStats totalStats = getStats(player);

        // 5. Create and return the structured breakdown object.
        return new StatBreakdown(baseStats, itemStats, skillStats, totalStats);
    }
}