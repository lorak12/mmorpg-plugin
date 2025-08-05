package org.nakii.mmorpg.managers;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class SkillManager {
    private final MMORPGCore plugin;
    private final Map<UUID, PlayerSkillData> skillDataMap = new HashMap<>();
    private FileConfiguration skillConfig;

    public SkillManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadSkillConfig();
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

        double currentExp = data.getExperience(skill);
        double expToLevel = getExperienceForLevel(skill, currentLevel);

        data.setExperience(skill, currentExp + amount);

        if (data.getExperience(skill) >= expToLevel) {
            data.setLevel(skill, currentLevel + 1);
            data.setExperience(skill, data.getExperience(skill) - expToLevel);
            player.sendMessage(ChatColor.AQUA + "You are now " + skill.name() + " level " + (currentLevel + 1) + "!");
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
}