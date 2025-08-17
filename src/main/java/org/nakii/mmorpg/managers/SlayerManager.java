package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.nakii.mmorpg.MMORPGCore;
import org.nakii.mmorpg.events.PlayerGainCombatXpEvent;
import org.nakii.mmorpg.events.SlayerProgressUpdateEvent;
import org.nakii.mmorpg.mob.CustomMobTemplate;
import org.nakii.mmorpg.scoreboard.ScoreboardProvider;
import org.nakii.mmorpg.slayer.ActiveSlayerQuest;
import org.nakii.mmorpg.slayer.BossSpawnAnimation;
import org.nakii.mmorpg.slayer.PlayerSlayerData;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class SlayerManager implements ScoreboardProvider, Listener {

    private final MMORPGCore plugin;
    private FileConfiguration slayerConfig;
    private final Map<UUID, ActiveSlayerQuest> activeQuests = new HashMap<>();
    private final SlayerDataManager slayerDataManager;

    public SlayerManager(MMORPGCore plugin) {
        this.plugin = plugin;
        this.slayerDataManager = plugin.getSlayerDataManager();
        loadSlayerConfig();
    }

    // --- Quest Progression (via Combat XP) ---
    @EventHandler
    public void onCombatXpGain(PlayerGainCombatXpEvent event) {
        Player player = event.getPlayer();
        ActiveSlayerQuest quest = getActiveSlayerQuest(player);
        if (quest == null || quest.getState() != ActiveSlayerQuest.QuestState.GATHERING_XP) {
            // This now correctly stops ANY further processing if the quest is not in the gathering state.
            return;
        }

        String victimId = plugin.getMobManager().getMobId(event.getVictim());
        if (victimId == null) return;
        CustomMobTemplate template = plugin.getMobManager().getTemplate(victimId);
        if (template == null) return;

        String requiredCategory = getTargetCategoryForQuest(quest);
        if (requiredCategory != null && requiredCategory.equalsIgnoreCase(template.getMobCategory())) {
            quest.addXp(event.getXpAmount());
            plugin.getServer().getPluginManager().callEvent(new SlayerProgressUpdateEvent(player, quest));

            // We only check for completion and spawn the boss inside this block.
            if (quest.isComplete()) {
                spawnSlayerBoss(player, quest);
                // We DO NOT end the quest here. We let the state change to BOSS_FIGHT.
            }
        }
    }

    public void addSlayerExperience(Player player, String slayerType, int amount) {
        PlayerSlayerData data = slayerDataManager.getData(player);
        if (data == null || amount <= 0) return;

        int oldLevel = data.getLevel(slayerType);
        data.addXp(slayerType, amount);
        player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>+"+amount+" "+slayerType.replace("_", " ")+" XP</gray>"));

        int newXp = data.getXp(slayerType);
        int newLevel = calculateLevelForXp(slayerType, newXp);

        if (newLevel > oldLevel) {
            data.setLevel(slayerType, newLevel);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>You reached "+slayerType.replace("_", " ")+" Level "+newLevel+"!</aqua>"));
            grantRewards(player, slayerType, newLevel);
        }
    }

    private void grantRewards(Player player, String slayerType, int level) {
        List<String> rewardStrings = slayerConfig.getStringList(slayerType + ".rewards." + level);
        for (String reward : rewardStrings) {
            // TODO: Implement reward parsing and granting logic
            System.out.println("Granting reward to " + player.getName() + ": " + reward);
        }
    }

    private int calculateLevelForXp(String slayerType, int currentXp) {
        ConfigurationSection xpSection = slayerConfig.getConfigurationSection(slayerType + ".leveling-xp");
        if (xpSection == null) return 0;
        int level = 0;
        for (String levelKey : xpSection.getKeys(false)) {
            int requiredXp = xpSection.getInt(levelKey);
            if (currentXp >= requiredXp) {
                level = Integer.parseInt(levelKey);
            } else {
                break;
            }
        }
        return level;
    }

    public int getSlayerLevel(Player player, String slayerType) {
        PlayerSlayerData data = slayerDataManager.getData(player);
        return (data != null) ? data.getLevel(slayerType) : 0;
    }

    // --- Core Quest Management ---
    public void startQuest(Player player, String slayerType, int tier) {
        if (hasActiveQuest(player)) return;
        int xpToSpawn = slayerConfig.getInt(slayerType + ".tiers." + tier + ".xp-to-spawn");
        ActiveSlayerQuest quest = new ActiveSlayerQuest(slayerType, tier, xpToSpawn);
        activeQuests.put(player.getUniqueId(), quest);
        plugin.getScoreboardManager().setActiveProvider(player, this);
    }

    // --- This method is now used for claiming rewards or cancelling ---
    public void endQuest(Player player) {
        activeQuests.remove(player.getUniqueId());
        plugin.getScoreboardManager().clearActiveProvider(player);
        try {
            plugin.getDatabaseManager().deleteActiveSlayerQuest(player.getUniqueId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasActiveQuest(Player player) {
        return activeQuests.containsKey(player.getUniqueId());
    }

    public ActiveSlayerQuest getActiveSlayerQuest(Player player) {
        return activeQuests.get(player.getUniqueId());
    }

    /**
     * Starts the spawn animation for a slayer boss. The actual mob spawn is handled by the animation task.
     */
    public void spawnSlayerBoss(Player player, ActiveSlayerQuest quest) {
        String path = quest.getSlayerType() + ".tiers." + quest.getTier();
        String bossId = slayerConfig.getString(path + ".boss.id");
        if (bossId != null) {
            ConfigurationSection bossConfig = slayerConfig.getConfigurationSection(path + ".boss");

            // --- MODIFIED: Start the animation instead of spawning directly ---
            new BossSpawnAnimation(plugin, player, player.getLocation(), bossId, bossConfig)
                    .runTaskTimer(plugin, 0L, 1L); // Run the animation every tick

            player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>A powerful entity is being summoned..."));
        }
    }

    /**
     * Called by BossSpawnAnimation when the animation is complete.
     * This method handles the final logic of spawning the mob and updating the quest state.
     */
    public void finalizeBossSpawn(Player player, ActiveSlayerQuest quest, String bossId, ConfigurationSection bossConfig, Location spawnLocation) {
        LivingEntity bossEntity = plugin.getMobManager().spawnMob(bossId, spawnLocation, bossConfig);
        if (bossEntity != null) {
            quest.setActiveBoss(bossEntity);
            quest.setState(ActiveSlayerQuest.QuestState.BOSS_FIGHT);
            plugin.getScoreboardManager().updateScoreboard(player);
            player.sendMessage("Slayer boss has spawned!");
        } else {
            // Failsafe in case the spawn fails after the animation
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The summoning failed!"));
            // You might want to refund the player or end the quest here.
            endQuest(player);
        }
    }

    // --- Configuration and Scoreboard ---
    private void loadSlayerConfig() {
        File file = new File(plugin.getDataFolder(), "slayers.yml");
        if (!file.exists()) {
            plugin.saveResource("slayers.yml", false);
        }
        this.slayerConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getSlayerConfig() {
        return slayerConfig;
    }

    public String getTargetCategoryForQuest(ActiveSlayerQuest quest) {
        return slayerConfig.getString(quest.getSlayerType() + ".target-category");
    }

    @Override
    public List<String> getScoreboardLines(Player player) {
        ActiveSlayerQuest quest = getActiveSlayerQuest(player);
        if (quest == null) return List.of();

        String bossName = getSlayerConfig().getString(quest.getSlayerType() + ".display-name");
        List<String> lines = new ArrayList<>();
        lines.add("<white>Slayer Quest</white>");
        lines.add(bossName);

        switch (quest.getState()) {
            case GATHERING_XP:
                lines.add(String.format("<white>(<yellow>%,.0f</yellow>/<red>%,d</red>) <gray>Combat XP</gray>",
                        quest.getCurrentXp(), quest.getXpToSpawn()));
                break;
            case BOSS_FIGHT:
                lines.add("<red><b>SLAY THE BOSS</b></red>");
                break;
            case AWAITING_CLAIM:
                lines.add("<green><b>CLAIM REWARDS</b></green>");
                break;
        }
        return lines;
    }

    public boolean isSlayerBossId(String mobId) {
        for (String slayerType : slayerConfig.getKeys(false)) {
            ConfigurationSection tiers = slayerConfig.getConfigurationSection(slayerType + ".tiers");
            if (tiers == null) continue;
            for (String tierKey : tiers.getKeys(false)) {
                if (mobId.equalsIgnoreCase(tiers.getString(tierKey + ".boss.id"))) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> getBossAbilitiesById(String mobId) {
        for (String slayerType : slayerConfig.getKeys(false)) {
            ConfigurationSection tiers = slayerConfig.getConfigurationSection(slayerType + ".tiers");
            if (tiers == null) continue;
            for (String tierKey : tiers.getKeys(false)) {
                if (mobId.equalsIgnoreCase(tiers.getString(tierKey + ".boss.id"))) {
                    return tiers.getStringList(tierKey + ".boss.abilities");
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Finds and returns the specific ConfigurationSection for a boss tier by its unique ID.
     * @param mobId The ID of the boss mob (e.g., "REVENANT_HORROR_1").
     * @return The ConfigurationSection for that boss tier, or null if not found.
     */
    public ConfigurationSection getBossConfigById(String mobId) {
        for (String slayerType : slayerConfig.getKeys(false)) {
            ConfigurationSection tiers = slayerConfig.getConfigurationSection(slayerType + ".tiers");
            if (tiers == null) continue;
            for (String tierKey : tiers.getKeys(false)) {
                ConfigurationSection bossSection = tiers.getConfigurationSection(tierKey + ".boss");
                if (bossSection != null && mobId.equalsIgnoreCase(bossSection.getString("id"))) {
                    return bossSection;
                }
            }
        }
        return null;
    }

    /**
     * Loads a player's quest from the database and puts it into the active cache.
     * Called by the PlayerConnectionListener on join.
     */
    public void loadQuestForPlayer(Player player) throws SQLException {
        Optional<ActiveSlayerQuest> questOpt = plugin.getDatabaseManager().loadActiveSlayerQuest(player.getUniqueId());
        questOpt.ifPresent(quest -> {
            activeQuests.put(player.getUniqueId(), quest);
            // Immediately set them as the provider so the scoreboard shows up
            plugin.getScoreboardManager().setActiveProvider(player, this);
        });
    }

    /**
     * Saves a player's active quest from the cache into the database.
     * Called by the PlayerConnectionListener on quit.
     */
    public void saveQuestForPlayer(Player player) throws SQLException {
        ActiveSlayerQuest quest = getActiveSlayerQuest(player);
        if (quest != null) {
            plugin.getDatabaseManager().saveActiveSlayerQuest(player.getUniqueId(), quest);
        } else {
            // If they have no active quest, ensure no old quest remains in the DB
            plugin.getDatabaseManager().deleteActiveSlayerQuest(player.getUniqueId());
        }
    }
}