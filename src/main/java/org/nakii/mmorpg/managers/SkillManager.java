package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
import java.util.concurrent.ConcurrentHashMap;

public class SkillManager {
    private final MMORPGCore plugin;
    private final Map<UUID, PlayerSkillData> playerDataCache = new HashMap<>();
    private FileConfiguration skillsConfig;
    private FileConfiguration levelsConfig;

    private final Map<Integer, Integer> xpToReachLevelCache = new TreeMap<>();
    private final Map<Skill, Map<Stat, Double>> skillRewardCache = new EnumMap<>(Skill.class);
    // --- NEW FIELDS FOR THE ADVANCED XP BUFFER ---

    // A thread-safe map to accumulate incoming XP gains before they are displayed.
    // Format: Player UUID -> {Skill -> Accumulated XP}
    private final Map<UUID, Map<Skill, Double>> pendingXpGains = new ConcurrentHashMap<>();

    // Tracks the currently displayed action bar message for each player to manage its lifecycle.
    private final Map<UUID, ActiveDisplayInfo> activeDisplays = new ConcurrentHashMap<>();

    // A simple record to hold the state of the currently showing action bar message.
    private record ActiveDisplayInfo(Skill skill, long expirationTime) {}

    // The BukkitTask that runs our display logic.
    private BukkitTask xpDisplayTask = null;

    // How long each XP bar message should stay on screen (in seconds).
    private static final int XP_BAR_DURATION_SECONDS = 2;

    private final DatabaseManager databaseManager;
    private final StatsManager statsManager;
    private final MobManager mobManager;
    private final HUDManager hudManager;

    // RewardManager is no longer final and will be injected.
    private RewardManager rewardManager;


    // Constructor is simplified.
    public SkillManager(MMORPGCore plugin, DatabaseManager databaseManager, StatsManager statsManager, MobManager mobManager, HUDManager hudManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.statsManager = statsManager;
        this.mobManager = mobManager;
        this.hudManager = hudManager;
        loadSkillsConfig();
        loadLevelsConfig();
        startXpDisplayTask();
    }

    // Setter for RewardManager
    public void setRewardManager(RewardManager rewardManager) {
        this.rewardManager = rewardManager;
    }

    private void loadSkillsConfig() {
        File file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) plugin.saveResource("skills.yml", false);
        this.skillsConfig = YamlConfiguration.loadConfiguration(file);

        // <<< FIX: Populate the cache when the config is loaded >>>
        skillRewardCache.clear();
        for (Skill skill : Skill.values()) {
            ConfigurationSection rewardsSection = skillsConfig.getConfigurationSection(skill.name() + ".rewards-per-level");
            if (rewardsSection != null) {
                Map<Stat, Double> rewards = new EnumMap<>(Stat.class);
                for (String statKey : rewardsSection.getKeys(false)) {
                    try {
                        Stat stat = Stat.valueOf(statKey.toUpperCase());
                        rewards.put(stat, rewardsSection.getDouble(statKey));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid stat '" + statKey + "' in rewards for skill " + skill.name());
                    }
                }
                skillRewardCache.put(skill, rewards);
            }
        }
        plugin.getLogger().info("Cached per-level rewards for " + skillRewardCache.size() + " skills.");
    }

    public Map<Stat, Double> getCachedSkillRewards(Skill skill) {
        return skillRewardCache.getOrDefault(skill, Collections.emptyMap());
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
        if (amount <= 0) return;

        PlayerSkillData data = getPlayerData(player);
        int currentLevel = data.getLevel(skill);
        int maxLevel = skillsConfig.getInt(skill.name() + ".max-level", 60);

        if (currentLevel >= maxLevel) return;

        // --- 1. IMMEDIATELY update the player's persistent data ---
        data.addXp(skill, amount);
        int newLevel = calculateLevelFromTotalXp(data.getXp(skill));

        // Level-up logic now runs on the main thread safely.
        if (newLevel > currentLevel) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerSkillData latestData = getPlayerData(player);
                    int latestLevel = latestData.getLevel(skill);
                    int calculatedNewLevel = calculateLevelFromTotalXp(latestData.getXp(skill));

                    if (calculatedNewLevel > latestLevel) {
                        latestData.setLevel(skill, calculatedNewLevel);
                        handleLevelUp(player, skill, calculatedNewLevel, latestLevel);
                    }
                }
            }.runTask(plugin);
        }

        // --- 2. Buffer the visual display ---
        // If a different skill's XP is currently being displayed, this will queue the new XP.
        // If the same skill is being displayed, this will just add to the next message's total.
        pendingXpGains.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .merge(skill, amount, Double::sum);
    }

    private void handleLevelUp(Player player, Skill skill, int newLevel, int oldLevel) {
        if (rewardManager == null) {
            plugin.getLogger().warning("RewardManager is not available...");
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        String skillDisplayName = skillsConfig.getString(skill.name() + ".display-name", skill.name());
        List<String> allRewardStrings = new ArrayList<>();
        int totalCoins = 0;

        for (int level = oldLevel + 1; level <= newLevel; level++) {
            totalCoins += levelsConfig.getInt("levels." + level + ".coins", 0);
            allRewardStrings.addAll(skillsConfig.getStringList(skill.name() + ".milestone-rewards." + level));
        }

        if (totalCoins > 0) {
            // Add coins to the list to be processed by RewardManager
            allRewardStrings.add("COINS:" + totalCoins);
        }

        // Grant the rewards FIRST
        rewardManager.grantRewards(player, allRewardStrings);

        // --- NEW: Build the announcement message using the formatted strings ---
        player.sendMessage(ChatUtils.format("<dark_gray>-----------------------------------"));
        player.sendMessage(ChatUtils.format("                <green><b>SKILL LEVEL UP</b></green>"));
        player.sendMessage(ChatUtils.format("   " + skillDisplayName + " <gray>" + FormattingUtils.toRoman(oldLevel) + " -> <yellow>" + FormattingUtils.toRoman(newLevel) + "</yellow>"));
        player.sendMessage(ChatUtils.format(" "));
        player.sendMessage(ChatUtils.format("  <white><b>REWARDS</b></white>"));

        // <<< FIX: Generate the chat message using the same formatter >>>
        for (String rewardString : allRewardStrings) {
            player.sendMessage(ChatUtils.format("  " + ChatUtils.formatRewardString(rewardString)));
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

    private void startXpDisplayTask() {
        if (xpDisplayTask != null) {
            xpDisplayTask.cancel();
        }
        // Run this task on the main server thread every 5 ticks. This is responsive and safe.
        xpDisplayTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    ActiveDisplayInfo activeDisplay = activeDisplays.get(uuid);

                    // Step 1: Check if the current message has expired.
                    if (activeDisplay != null && System.currentTimeMillis() > activeDisplay.expirationTime()) {
                        activeDisplays.remove(uuid);
                        activeDisplay = null; // It's expired, so we can show a new one.
                    }

                    // Step 2: If nothing is currently being shown, check for pending XP to display.
                    if (activeDisplay == null) {
                        Map<Skill, Double> playerPendingGains = pendingXpGains.get(uuid);
                        if (playerPendingGains != null && !playerPendingGains.isEmpty()) {

                            // Pick the first available skill from the pending map to display.
                            Skill skillToShow = playerPendingGains.keySet().iterator().next();
                            double accumulatedXp = playerPendingGains.remove(skillToShow);

                            // If the map for this player is now empty, remove them to save memory.
                            if (playerPendingGains.isEmpty()) {
                                pendingXpGains.remove(uuid);
                            }

                            // Step 3: Build and display the new message.
                            PlayerSkillData data = getPlayerData(player);
                            int level = data.getLevel(skillToShow);
                            double totalXp = data.getXp(skillToShow);
                            double xpForCurrentLevel = getCumulativeXpForLevel(level);
                            double xpForNextLevel = getCumulativeXpForLevel(level + 1);

                            double progressInLevel = totalXp - xpForCurrentLevel;
                            double neededForLevel = xpForNextLevel - xpForCurrentLevel;

                            // Format: +36 ⚔ Combat XP (36/125)
                            String message = String.format("<aqua>+%,.0f %s %s XP</aqua> <gray>(<yellow>%,.0f</yellow>/<green>%,.0f</green>)</gray>",
                                    accumulatedXp,
                                    skillToShow.getSymbol(),
                                    ChatUtils.capitalizeWords(skillToShow.name()),
                                    progressInLevel,
                                    neededForLevel
                            );

                            // Send the message and register it as the active display.
                            hudManager.updateActionBar(player, message, XP_BAR_DURATION_SECONDS);
                            long expiration = System.currentTimeMillis() + (XP_BAR_DURATION_SECONDS * 1000L);
                            activeDisplays.put(uuid, new ActiveDisplayInfo(skillToShow, expiration));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L); // Run every 1/4 of a second (5 ticks)
    }


    public FileConfiguration getSkillsConfig() { return skillsConfig; }
    public FileConfiguration getLevelsConfig() { return levelsConfig; }
}