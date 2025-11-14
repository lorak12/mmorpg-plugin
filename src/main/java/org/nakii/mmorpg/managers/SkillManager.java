package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.events.PlayerGainCombatXpEvent;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.player.PlayerStats;
import org.nakii.mmorpg.player.Stat;
import org.nakii.mmorpg.skills.PlayerSkillData;
import org.nakii.mmorpg.skills.Skill;
import org.nakii.mmorpg.util.ChatUtils;
import org.nakii.mmorpg.util.FormattingUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class SkillManager {
    private final MMORPGCore plugin;
    private final Map<UUID, PlayerSkillData> playerDataCache = new HashMap<>();
    private FileConfiguration skillsConfig;
    private FileConfiguration levelsConfig;

    private final DatabaseManager databaseManager;
    private final StatsManager statsManager;
    private final MobManager mobManager;
    private final HUDManager hudManager;

    // RewardManager is no longer final and will be injected.
    private RewardManager rewardManager;

    private final Map<Integer, Integer> xpToReachLevelCache = new TreeMap<>();

    // Constructor is simplified.
    public SkillManager(MMORPGCore plugin, DatabaseManager databaseManager, StatsManager statsManager, MobManager mobManager, HUDManager hudManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.statsManager = statsManager;
        this.mobManager = mobManager;
        this.hudManager = hudManager;
        loadSkillsConfig();
        loadLevelsConfig();
    }

    // Setter for RewardManager
    public void setRewardManager(RewardManager rewardManager) {
        this.rewardManager = rewardManager;
    }

    private void loadSkillsConfig() {
        File file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) plugin.saveResource("skills.yml", false);
        this.skillsConfig = YamlConfiguration.loadConfiguration(file);
    }

    private void loadLevelsConfig() {
        File file = new File(plugin.getDataFolder(), "levels.yml");
        if (!file.exists()) plugin.saveResource("levels.yml", false);
        this.levelsConfig = YamlConfiguration.loadConfiguration(file);

        xpToReachLevelCache.clear();
        ConfigurationSection levels = levelsConfig.getConfigurationSection("levels");
        if (levels != null) {
            for (String key : levels.getKeys(false)) {
                try {
                    xpToReachLevelCache.put(Integer.parseInt(key), levels.getInt(key + ".cumulative_xp"));
                } catch (NumberFormatException ignored) {}
            }
        }
        plugin.getLogger().info("Loaded " + xpToReachLevelCache.size() + " level progression steps.");
    }

    public void addXp(Player player, Skill skill, double amount) {
        PlayerSkillData data = getPlayerData(player);
        int currentLevel = data.getLevel(skill);
        int maxLevel = skillsConfig.getInt(skill.name() + ".max-level", 60);

        if (currentLevel >= maxLevel) return;

        double oldTotalXp = data.getXp(skill);
        data.addXp(skill, amount);
        double newTotalXp = data.getXp(skill);

        int newLevel = calculateLevelFromTotalXp(newTotalXp);

        if (newLevel > currentLevel) {
            data.setLevel(skill, newLevel);
            handleLevelUp(player, skill, newLevel, currentLevel);
        }

        hudManager.updateActionBar(player, "<aqua>+" + amount + " " + skill.name() + " XP</aqua>", 3);
    }

    private void handleLevelUp(Player player, Skill skill, int newLevel, int oldLevel) {
        // Null-check for RewardManager, crucial during initial load or reloads.
        if(rewardManager == null) {
            plugin.getLogger().warning("RewardManager is not available, cannot grant level-up rewards for " + player.getName());
            return;
        }

        String skillDisplayName = skillsConfig.getString(skill.name() + ".display-name", skill.name());
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        List<String> allRewardStrings = new ArrayList<>();
        int totalCoins = 0;

        for (int level = oldLevel + 1; level <= newLevel; level++) {
            totalCoins += levelsConfig.getInt("levels." + level + ".coins", 0);
            allRewardStrings.addAll(skillsConfig.getStringList(skill.name() + ".milestone-rewards." + level));
        }

        if (totalCoins > 0) {
            allRewardStrings.add("COINS:" + totalCoins);
        }

        player.sendMessage(ChatUtils.format("<dark_gray>-----------------------------------"));
        player.sendMessage(ChatUtils.format("                <green><b>LEVEL UP</b></green>"));
        player.sendMessage(ChatUtils.format("   " + skillDisplayName + " <gray>" + FormattingUtils.toRoman(oldLevel) + " -> <yellow>" + FormattingUtils.toRoman(newLevel) + "</yellow>"));
        player.sendMessage(ChatUtils.format(" "));
        player.sendMessage(ChatUtils.format("  <white>Rewards:"));

        List<Component> rewardComponents = rewardManager.grantRewards(player, allRewardStrings);
        for (Component rewardLine : rewardComponents) {
            player.sendMessage(Component.text("  ").append(rewardLine));
        }

        player.sendMessage(ChatUtils.format("<dark_gray>-----------------------------------"));
        statsManager.recalculateStats(player);
    }

    public void handleMobKill(Player player, LivingEntity victim) {
        double baseXP = 0.0;
        String mobId = mobManager.getMobId(victim);
        if (mobId != null) {
            CustomMobTemplate template = mobManager.getTemplate(mobId);
            if (template != null) {
                baseXP = (template.getStat(Stat.HEALTH) / 20.0) + (template.getLevel() * 2.5);
            }
        } else {
            baseXP = 5.0; // Default for vanilla mobs
        }

        if (baseXP <= 0) return;

        PlayerStats playerStats = statsManager.getStats(player);
        double combatWisdom = playerStats.getStat(Stat.COMBAT_WISDOM);
        double finalXP = baseXP * (1 + (combatWisdom / 100.0));

        PlayerGainCombatXpEvent event = new PlayerGainCombatXpEvent(player, victim, finalXP);
        plugin.getServer().getPluginManager().callEvent(event);

        addXp(player, Skill.COMBAT, event.getXpAmount());
    }

    public void loadPlayerData(Player player) {
        try {
            PlayerSkillData data = databaseManager.loadPlayerSkillData(player.getUniqueId());
            playerDataCache.put(player.getUniqueId(), data);
        } catch (SQLException e) {
            e.printStackTrace();
            playerDataCache.put(player.getUniqueId(), new PlayerSkillData());
        }
    }

    public void savePlayerData(Player player) {
        if (playerDataCache.containsKey(player.getUniqueId())) {
            try {
                databaseManager.savePlayerSkillData(player.getUniqueId(), getPlayerData(player));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public PlayerSkillData getPlayerData(Player player) { return playerDataCache.get(player.getUniqueId()); }
    public int getLevel(Player player, Skill skill) { return getPlayerData(player).getLevel(skill); }
    public double getTotalXp(Player player, Skill skill) { return getPlayerData(player).getXp(skill); }

    public int calculateLevelFromTotalXp(double totalXp) {
        int level = 0;
        for (Map.Entry<Integer, Integer> entry : xpToReachLevelCache.entrySet()) {
            if (totalXp >= entry.getValue()) {
                level = Math.max(level, entry.getKey());
            } else {
                break;
            }
        }
        return level;
    }

    public void addXpForAction(Player player, Skill skill, String actionKey) {
        double xpAmount = skillsConfig.getDouble(skill.name() + ".xp-sources." + actionKey.toUpperCase(), 0.0);
        if (xpAmount > 0) {
            addXp(player, skill, xpAmount);
        }
    }

    public int getCumulativeXpForLevel(int level) {
        return xpToReachLevelCache.getOrDefault(level, 0);
    }

    public FileConfiguration getSkillsConfig() { return skillsConfig; }
    public FileConfiguration getLevelsConfig() { return levelsConfig; }
}