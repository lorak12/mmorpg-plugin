package org.nakii.mmorpg.managers;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.events.PlayerGainCombatXpEvent;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.utils.ChatUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkillManager {
    private final MMORPGCore plugin;
    private final Map<UUID, PlayerSkillData> skillDataMap = new HashMap<>();
    private FileConfiguration skillConfig;

    // --- NEW FIELDS ---
    private String xpFormula;
    private final Map<Skill, Integer> maxLevels = new EnumMap<>(Skill.class);
    private final Map<Skill, Map<String, Double>> skillXpSources = new EnumMap<>(Skill.class);

    public SkillManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadSkillsConfig();
    }

    public void loadSkillsConfig() {
        File skillsFile = new File(plugin.getDataFolder(), "skills.yml");
        if (!skillsFile.exists()) {
            plugin.saveResource("skills.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(skillsFile);

        this.xpFormula = config.getString("xp-formula", "100 * (level ^ 2.5)");

        ConfigurationSection skillsSection = config.getConfigurationSection("skills");
        if (skillsSection == null) return;

        for (String skillKey : skillsSection.getKeys(false)) {
            try {
                Skill skill = Skill.valueOf(skillKey.toUpperCase());
                ConfigurationSection skillConfig = skillsSection.getConfigurationSection(skillKey);

                maxLevels.put(skill, skillConfig.getInt("max-level", 50));

                Map<String, Double> xpSources = new HashMap<>();
                ConfigurationSection sourcesSection = skillConfig.getConfigurationSection("xp-sources");
                if (sourcesSection != null) {
                    for (String sourceKey : sourcesSection.getKeys(false)) {
                        xpSources.put(sourceKey.toUpperCase(), sourcesSection.getDouble(sourceKey));
                    }
                }
                skillXpSources.put(skill, xpSources);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid skill '" + skillKey + "' found in skills.yml.");
            }
        }
        plugin.getLogger().info("Loaded skill configurations for " + skillXpSources.size() + " skills.");
    }

    /**
     * Calculates the total XP required to reach a certain level using the exp4j library.
     */
    public double getTotalXpForLevel(int level) {
        if (level <= 1) return 0;
        try {
            // Create an expression with a variable 'level'
            Expression expression = new ExpressionBuilder(this.xpFormula)
                    .variable("level")
                    .build()
                    .setVariable("level", level);

            return expression.evaluate();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to evaluate XP formula for level " + level + ": " + e.getMessage());
            return Double.MAX_VALUE;
        }
    }

    /**
     * Gets the XP required to go from the previous level to the target level.
     */
    public double getXpForLevel(int level) {
        if (level <= 1) return getTotalXpForLevel(1);
        return getTotalXpForLevel(level) - getTotalXpForLevel(level - 1);
    }

    public void addXp(Player player, Skill skill, double amount) {
        PlayerSkillData data = getSkillData(player);
        int currentLevel = data.getLevel(skill);

        if (currentLevel >= maxLevels.getOrDefault(skill, 50)) {
            return; // Player is at max level for this skill
        }

        data.addXp(skill, amount);

        double currentXpInLevel = data.getXp(skill);
        double xpForNextLevel = getXpForLevel(currentLevel + 1);

        while (currentXpInLevel >= xpForNextLevel) {
            currentLevel++;
            data.setLevel(skill, currentLevel);
            currentXpInLevel -= xpForNextLevel;
            data.setXp(skill, currentXpInLevel);

            // Announce level up
            player.sendMessage(ChatUtils.format("<green>Your " + ChatUtils.capitalizeWords(skill.name()) + " skill is now level " + currentLevel + "!</green>"));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            plugin.getStatsManager().recalculateStats(player);

            if (currentLevel >= maxLevels.getOrDefault(skill, 50)) {
                data.setXp(skill, 0); // At max level, reset XP in level to 0
                break; // Exit the loop
            }
            xpForNextLevel = getXpForLevel(currentLevel + 1);
        }
    }

    public void handleBlockBreak(Player player, Material material) {
        for (Map.Entry<Skill, Map<String, Double>> entry : skillXpSources.entrySet()) {
            Skill skill = entry.getKey();
            Map<String, Double> sources = entry.getValue();
            if (sources.containsKey(material.name())) {
                addXp(player, skill, sources.get(material.name()));
            }
        }
    }

    public void handleMobKill(Player player, LivingEntity victim) {
        double baseXP = 0.0;

        // --- 1. Identify the Mob and Calculate Base XP ---
        String mobId = plugin.getMobManager().getMobId(victim);

        if (mobId != null) {
            // It's a custom mob. Use our dynamic formula.
            CustomMobTemplate template = plugin.getMobManager().getTemplate(mobId);
            if (template != null) {
                double health = template.getStat(Stat.HEALTH);
                int level = template.getLevel();
                // This is the formula for dynamic XP based on mob stats.
                baseXP = (health / 20.0) + (level * 2.5);
            }
        } else {
            // It's a vanilla mob. Use the old system as a fallback.
            EntityType entityType = victim.getType();
            Map<String, Double> combatSources = skillXpSources.get(Skill.COMBAT);
            if (combatSources != null && combatSources.containsKey(entityType.name())) {
                baseXP = combatSources.get(entityType.name());
            }
        }

        // If no XP source was found, exit.
        if (baseXP <= 0) {
            return;
        }

        // --- 2. Apply Player's Combat Wisdom ---
        PlayerStats playerStats = plugin.getStatsManager().getStats(player);
        double combatWisdom = playerStats.getStat(Stat.COMBAT_WISDOM);
        // The formula for applying the percentage bonus from wisdom.
        double finalXP = baseXP * (1 + (combatWisdom / 100.0));

        // --- 3. Broadcast the Event with the Final XP Amount ---
        PlayerGainCombatXpEvent event = new PlayerGainCombatXpEvent(player, victim, finalXP);
        plugin.getServer().getPluginManager().callEvent(event);

        // --- 4. Grant the XP ---
        // We use the amount from the event, as another system could potentially modify it.
        addXp(player, Skill.COMBAT, event.getXpAmount());
    }

    public void loadSkillConfig() {
        File skillFile = new File(plugin.getDataFolder(), "skills.yml");
        if (!skillFile.exists()) {
            plugin.saveResource("skills.yml", false);
        }
        skillConfig = YamlConfiguration.loadConfiguration(skillFile);
    }

    /**
     * BUG FIX: Added public getter for the skill configuration.
     * This allows other classes to access the loaded skills.yml file.
     *
     * @return The loaded skill configuration.
     */
    public FileConfiguration getSkillConfig() {
        return skillConfig;
    }

    public void loadPlayerData(Player player) {
        try {
            PlayerSkillData data = plugin.getDatabaseManager().loadPlayerSkillData(player.getUniqueId());
            skillDataMap.put(player.getUniqueId(), data);
        } catch (SQLException e) {
            e.printStackTrace();
            skillDataMap.put(player.getUniqueId(), new PlayerSkillData());
        }
    }

    public void savePlayerData(Player player) {
        if (skillDataMap.containsKey(player.getUniqueId())) {
            try {
                plugin.getDatabaseManager().savePlayerSkillData(player.getUniqueId(), getSkillData(player));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public PlayerSkillData getSkillData(Player player) {
        return skillDataMap.getOrDefault(player.getUniqueId(), new PlayerSkillData());
    }

    public void addExperience(Player player, Skill skill, double amount) {
        PlayerSkillData data = getSkillData(player);
        int currentLevel = data.getLevel(skill);
        int maxLevel = getSkillConfig().getInt("skills." + skill.name().toLowerCase() + ".max_level", 50);

        if (currentLevel >= maxLevel) return;

        double currentExp = data.getXp(skill);
        double expToLevel = getExperienceForLevel(skill, currentLevel);

        data.setXp(skill, currentExp + amount);

        if (data.getXp(skill) >= expToLevel) {
            data.setLevel(skill, currentLevel + 1);
            data.setXp(skill, data.getXp(skill) - expToLevel);
            player.sendMessage(ChatUtils.format( "<aqua>You are now " + skill.name() + " level " + (currentLevel + 1) + "!") );
            plugin.getStatsManager().recalculateStats(player);
        }
    }

    public double getExperienceForLevel(Skill skill, int level) {
        String formula = getSkillConfig().getString("skills." + skill.name().toLowerCase() + ".experience_formula", "100 * %level%");
        // This is still a simple parser. A proper library like exp4j would be better for complex formulas.
        String replaced = formula.replace("%level%", String.valueOf(level));
        try {
            // Simple multiplication support
            if (replaced.contains("*")) {
                String[] parts = replaced.split("\\*");
                double value = 1;
                for (String part : parts) {
                    value *= Double.parseDouble(part.trim());
                }
                return value;
            }
            return Double.parseDouble(replaced);
        } catch (NumberFormatException e) {
            return 100 * level * level; // Fallback
        }
    }

    /**
     * REVISED: Gets the current level of a specific skill for a player.
     * This now correctly uses the unified data structure.
     */
    public int getLevel(Player player, Skill skill) {
        PlayerSkillData data = skillDataMap.get(player.getUniqueId());
        if (data == null) {
            // This can happen if a player's data isn't loaded properly.
            // Returning 0 is a safe fallback.
            return 0;
        }
        return data.getLevel(skill);
    }
}