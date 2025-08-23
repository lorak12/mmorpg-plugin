package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
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
import org.nakii.mmorpg.utils.ChatUtils;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

public class SkillManager {
    private final MMORPGCore plugin;
    private final Map<UUID, PlayerSkillData> playerDataCache = new HashMap<>();
    private FileConfiguration skillsConfig;
    private FileConfiguration levelsConfig;

    // A cache for level progression data for extremely fast lookups.
    private final Map<Integer, Integer> xpToReachLevelCache = new TreeMap<>();

    public SkillManager(MMORPGCore plugin) {
        this.plugin = plugin;
        loadSkillsConfig();
        loadLevelsConfig();
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

        // TODO: Send an action bar message to the player showing "+X Skill XP"
    }

    private void handleLevelUp(Player player, Skill skill, int newLevel, int oldLevel) {
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

        // --- Chat Message Generation ---
        player.sendMessage(ChatUtils.format("<dark_gray>-----------------------------------"));
        player.sendMessage(ChatUtils.format("                <green><b>LEVEL UP</b></green>"));
        player.sendMessage(ChatUtils.format("   " + skillDisplayName + " <gray>" + toRoman(oldLevel) + " -> <yellow>" + toRoman(newLevel) + "</yellow>"));
        player.sendMessage(ChatUtils.format(" "));
        player.sendMessage(ChatUtils.format("  <white>Rewards:"));

        // Grant rewards and simultaneously format them for the message
        List<Component> rewardComponents = plugin.getRewardManager().grantRewards(player, allRewardStrings);
        for (Component rewardLine : rewardComponents) {
            player.sendMessage(Component.text("  ").append(rewardLine));
        }

        player.sendMessage(ChatUtils.format("<dark_gray>-----------------------------------"));

        // Recalculate stats AFTER granting all rewards
        plugin.getStatsManager().recalculateStats(player);
    }

    public void handleMobKill(Player player, LivingEntity victim) {
        // This method is already correct and does not need to be changed.
        // It correctly calculates dynamic XP and fires the PlayerGainCombatXpEvent.
        // The final addXp call will now use our new, robust leveling system.
        double baseXP = 0.0;
        String mobId = plugin.getMobManager().getMobId(victim);
        if (mobId != null) {
            CustomMobTemplate template = plugin.getMobManager().getTemplate(mobId);
            if (template != null) {
                baseXP = (template.getStat(Stat.HEALTH) / 20.0) + (template.getLevel() * 2.5);
            }
        } else {
            baseXP = 5.0; // Default for vanilla mobs
        }

        if (baseXP <= 0) return;

        PlayerStats playerStats = plugin.getStatsManager().getStats(player);
        double combatWisdom = playerStats.getStat(Stat.COMBAT_WISDOM);
        double finalXP = baseXP * (1 + (combatWisdom / 100.0));

        PlayerGainCombatXpEvent event = new PlayerGainCombatXpEvent(player, victim, finalXP);
        plugin.getServer().getPluginManager().callEvent(event);

        addXp(player, Skill.COMBAT, event.getXpAmount());
    }

    // --- Data Management & Public Getters ---

    public void loadPlayerData(Player player) {
        try {
            PlayerSkillData data = plugin.getDatabaseManager().loadPlayerSkillData(player.getUniqueId());
            playerDataCache.put(player.getUniqueId(), data);
        } catch (SQLException e) {
            e.printStackTrace();
            playerDataCache.put(player.getUniqueId(), new PlayerSkillData());
        }
    }

    public void savePlayerData(Player player) {
        if (playerDataCache.containsKey(player.getUniqueId())) {
            try {
                plugin.getDatabaseManager().savePlayerSkillData(player.getUniqueId(), getPlayerData(player));
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
        // Example actionKey could be a Material name like "OAK_LOG" or a potion name
        double xpAmount = skillsConfig.getDouble(skill.name() + ".xp-sources." + actionKey.toUpperCase(), 0.0);
        if (xpAmount > 0) {
            addXp(player, skill, xpAmount);
        }
    }

    private String toRoman(int number) {
        if (number < 1 || number > 39) return String.valueOf(number);
        String[] r = {"X", "IX", "V", "IV", "I"};
        int[] v = {10, 9, 5, 4, 1};
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<v.length; i++) {
            while(number >= v[i]) {
                number -= v[i];
                sb.append(r[i]);
            }
        }
        return sb.toString();
    }

    public int getCumulativeXpForLevel(int level) {
        return xpToReachLevelCache.getOrDefault(level, 0);
    }

    public FileConfiguration getSkillsConfig() { return skillsConfig; }
    public FileConfiguration getLevelsConfig() { return levelsConfig; }
}