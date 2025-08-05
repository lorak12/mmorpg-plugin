package org.nakii.mmorpg.managers;

import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.stats.PlayerStats;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

    private void addStatsFromItem(PlayerStats stats, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Check if it's one of our custom items
        if (!container.has(new NamespacedKey(plugin, "item_id"), PersistentDataType.STRING)) {
            return;
        }

        // Add each stat from the item to the player's total stats
        stats.setHealth(stats.getHealth() + getStatFromItem(container, "health"));
        stats.setStrength(stats.getStrength() + getStatFromItem(container, "strength"));
        stats.setCritChance(stats.getCritChance() + getStatFromItem(container, "crit_chance"));
        stats.setCritDamage(stats.getCritDamage() + getStatFromItem(container, "crit_damage"));
        stats.setSpeed(stats.getSpeed() + getStatFromItem(container, "speed"));
        stats.setLuck(stats.getLuck() + getStatFromItem(container, "luck"));
        stats.setLethality(stats.getLethality() + getStatFromItem(container, "lethality"));
        stats.setTenacity(stats.getTenacity() + getStatFromItem(container, "tenacity"));
        stats.setArmorPenetration(stats.getArmorPenetration() + getStatFromItem(container, "armor_penetration"));
        stats.setArmor(stats.getArmor() + getStatFromItem(container, "armor"));
        stats.setMana(stats.getMana() + getStatFromItem(container, "mana"));
        stats.setManaRegen(stats.getManaRegen() + getStatFromItem(container, "mana_regen"));
        stats.setHpRegen(stats.getHpRegen() + getStatFromItem(container, "hp_regen"));
    }

    private double getStatFromItem(PersistentDataContainer container, String statName) {
        return container.getOrDefault(new NamespacedKey(plugin, "stat_" + statName), PersistentDataType.DOUBLE, 0.0);
    }

    private void applyVanillaAttributes(Player player, PlayerStats stats) {
        // Convert our speed stat (base 100) to Minecraft's walk speed (base 0.2)
        double minecraftSpeed = 0.2 * (stats.getSpeed() / 100.0);
        player.setWalkSpeed((float) minecraftSpeed);

        // Update player's max health
        // Note: The custom health system will override this, but it's good practice.
        player.setMaxHealth(stats.getHealth());
    }

    /**
     * Recalculates all stats for a player from base, items, and skills.
     */
    public void recalculateStats(Player player) {
        PlayerStats finalStats = new PlayerStats(); // Start with fresh base stats
        PlayerStats itemStats = new PlayerStats(true); // Empty stats for items
        PlayerStats skillStats = new PlayerStats(true); // Empty stats for skills

        // Calculate skill stats
        calculateSkillStats(player, skillStats);

        // Calculate item stats
        PlayerInventory inventory = player.getInventory();
        addStatsFromItem(itemStats, inventory.getItemInMainHand());
        addStatsFromItem(itemStats, inventory.getItemInOffHand());
        for (ItemStack armor : inventory.getArmorContents()) {
            addStatsFromItem(itemStats, armor);
        }

        // Combine all stats
        finalStats.addHealth(itemStats.getHealth() + skillStats.getHealth());
        finalStats.addStrength(itemStats.getStrength() + skillStats.getStrength());
        finalStats.addCritChance(itemStats.getCritChance() + skillStats.getCritChance());
        finalStats.addCritDamage(itemStats.getCritDamage() + skillStats.getCritDamage());
        finalStats.addSpeed(itemStats.getSpeed() + skillStats.getSpeed());
        finalStats.addLuck(itemStats.getLuck() + skillStats.getLuck());
        finalStats.addLethality(itemStats.getLethality() + skillStats.getLethality());
        finalStats.addTenacity(itemStats.getTenacity() + skillStats.getTenacity());
        finalStats.addArmorPenetration(itemStats.getArmorPenetration() + skillStats.getArmorPenetration());
        finalStats.addArmor(itemStats.getArmor() + skillStats.getArmor());
        finalStats.addMana(itemStats.getMana() + skillStats.getMana());
        finalStats.addManaRegen(itemStats.getManaRegen() + skillStats.getManaRegen());
        finalStats.addHpRegen(itemStats.getHpRegen() + skillStats.getHpRegen());

        playerStatsMap.put(player.getUniqueId(), finalStats);
        applyVanillaAttributes(player, finalStats);
    }

    /**
     * Generates a detailed breakdown of a player's stats for GUI display.
     */
    public StatBreakdown getStatBreakdown(Player player) {
        StatBreakdown breakdown = new StatBreakdown();
        PlayerStats baseStats = new PlayerStats();
        PlayerStats itemStats = new PlayerStats(true);
        PlayerStats skillStats = new PlayerStats(true);

        // Step 1: Populate Base and Skill contributions
        breakdown.setBaseHealth(baseStats.getHealth());
        breakdown.setBaseStrength(baseStats.getStrength());
        breakdown.setBaseCritChance(baseStats.getCritChance());
        breakdown.setBaseCritDamage(baseStats.getCritDamage());
        breakdown.setBaseSpeed(baseStats.getSpeed());
        breakdown.setBaseLuck(baseStats.getLuck());
        breakdown.setBaseLethality(baseStats.getLethality());
        breakdown.setBaseTenacity(baseStats.getTenacity());
        breakdown.setBaseArmorPen(baseStats.getArmorPenetration());
        breakdown.setBaseArmor(baseStats.getArmor());
        breakdown.setBaseMana(baseStats.getMana());
        breakdown.setBaseManaRegen(baseStats.getManaRegen());
        breakdown.setBaseHpRegen(baseStats.getHpRegen());

        calculateSkillStats(player, skillStats);
        breakdown.setSkillHealth(skillStats.getHealth());
        breakdown.setSkillStrength(skillStats.getStrength());
        breakdown.setSkillCritChance(skillStats.getCritChance());
        breakdown.setSkillCritDamage(skillStats.getCritDamage());
        breakdown.setSkillSpeed(skillStats.getSpeed());
        breakdown.setSkillLuck(skillStats.getLuck());
        breakdown.setSkillLethality(skillStats.getLethality());
        breakdown.setSkillTenacity(skillStats.getTenacity());
        breakdown.setSkillArmorPen(skillStats.getArmorPenetration());
        breakdown.setSkillArmor(skillStats.getArmor());
        breakdown.setSkillMana(skillStats.getMana());
        breakdown.setSkillManaRegen(skillStats.getManaRegen());
        breakdown.setSkillHpRegen(skillStats.getHpRegen());

        // Step 2: Populate Item contributions
        PlayerInventory inventory = player.getInventory();
        addStatsFromItem(itemStats, inventory.getItemInMainHand());
        addStatsFromItem(itemStats, inventory.getItemInOffHand());
        for (ItemStack armor : inventory.getArmorContents()) {
            addStatsFromItem(itemStats, armor);
        }
        breakdown.setItemHealth(itemStats.getHealth());
        breakdown.setItemStrength(itemStats.getStrength());
        breakdown.setItemCritChance(itemStats.getCritChance());
        breakdown.setItemCritDamage(itemStats.getCritDamage());
        breakdown.setItemSpeed(itemStats.getSpeed());
        breakdown.setItemLuck(itemStats.getLuck());
        breakdown.setItemLethality(itemStats.getLethality());
        breakdown.setItemTenacity(itemStats.getTenacity());
        breakdown.setItemArmorPen(itemStats.getArmorPenetration());
        breakdown.setItemArmor(itemStats.getArmor());
        breakdown.setItemMana(itemStats.getMana());
        breakdown.setItemManaRegen(itemStats.getManaRegen());
        breakdown.setItemHpRegen(itemStats.getHpRegen());

        // Step 3: Calculate Totals
        breakdown.setTotalHealth(breakdown.getBaseHealth() + breakdown.getItemHealth() + breakdown.getSkillHealth());
        breakdown.setTotalStrength(breakdown.getBaseStrength() + breakdown.getItemStrength() + breakdown.getSkillStrength());
        breakdown.setTotalCritChance(breakdown.getBaseCritChance() + breakdown.getItemCritChance() + breakdown.getSkillCritChance());
        breakdown.setTotalCritDamage(breakdown.getBaseCritDamage() + breakdown.getItemCritDamage() + breakdown.getSkillCritDamage());
        breakdown.setTotalSpeed(breakdown.getBaseSpeed() + breakdown.getItemSpeed() + breakdown.getSkillSpeed());
        breakdown.setTotalLuck(breakdown.getBaseLuck() + breakdown.getItemLuck() + breakdown.getSkillLuck());
        breakdown.setTotalLethality(breakdown.getBaseLethality() + breakdown.getItemLethality() + breakdown.getSkillLethality());
        breakdown.setTotalTenacity(breakdown.getBaseTenacity() + breakdown.getItemTenacity() + breakdown.getSkillTenacity());
        breakdown.setTotalArmorPen(breakdown.getBaseArmorPen() + breakdown.getItemArmorPen() + breakdown.getSkillArmorPen());
        breakdown.setTotalArmor(breakdown.getBaseArmor() + breakdown.getItemArmor() + breakdown.getSkillArmor());
        breakdown.setTotalMana(breakdown.getBaseMana() + breakdown.getItemMana() + breakdown.getSkillMana());
        breakdown.setTotalManaRegen(breakdown.getBaseManaRegen() + breakdown.getItemManaRegen() + breakdown.getSkillManaRegen());
        breakdown.setTotalHpRegen(breakdown.getBaseHpRegen() + breakdown.getItemHpRegen() + breakdown.getSkillHpRegen());

        return breakdown;
    }

    private void calculateSkillStats(Player player, PlayerStats skillStats) {
        PlayerSkillData skillData = plugin.getSkillManager().getSkillData(player);

        // This is where you pull the stat reward values from your skills.yml configuration
        skillStats.addStrength((skillData.getLevel(Skill.COMBAT) - 1) * plugin.getSkillManager().getSkillConfig().getDouble("skills.combat.rewards.strength", 0.0));
        skillStats.addLethality((skillData.getLevel(Skill.ARCHERY) - 1) * plugin.getSkillManager().getSkillConfig().getDouble("skills.archery.rewards.lethality", 0.0));
        skillStats.addHpRegen((skillData.getLevel(Skill.FARMING) - 1) * plugin.getSkillManager().getSkillConfig().getDouble("skills.farming.rewards.hp_regen", 0.0));
        skillStats.addManaRegen((skillData.getLevel(Skill.ALCHEMY) - 1) * plugin.getSkillManager().getSkillConfig().getDouble("skills.alchemy.rewards.mana_regen", 0.0));
        skillStats.addLuck((skillData.getLevel(Skill.FISHING) - 1) * plugin.getSkillManager().getSkillConfig().getDouble("skills.fishing.rewards.luck", 0.0));
        skillStats.addMana((skillData.getLevel(Skill.MAGIC) - 1) * plugin.getSkillManager().getSkillConfig().getDouble("skills.magic.rewards.mana", 0.0));
        skillStats.addArmor((skillData.getLevel(Skill.FORGING) - 1) * plugin.getSkillManager().getSkillConfig().getDouble("skills.forging.rewards.armor", 0.0));
    }
}