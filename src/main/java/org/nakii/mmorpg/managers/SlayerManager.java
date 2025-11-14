package org.nakii.mmorpg.managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
    private final DatabaseManager databaseManager;
    private final MobManager mobManager;
    private final ScoreboardManager scoreboardManager;
    private final LootManager lootManager;
    private final CollectionManager collectionManager;
    private final SkillManager skillManager;

    public SlayerManager(MMORPGCore plugin, DatabaseManager databaseManager, SlayerDataManager slayerDataManager, MobManager mobManager, ScoreboardManager scoreboardManager, LootManager lootManager, CollectionManager collectionManager, SkillManager skillManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.slayerDataManager = slayerDataManager;
        this.mobManager = mobManager;
        this.scoreboardManager = scoreboardManager;
        this.lootManager = lootManager;
        this.collectionManager = collectionManager;
        this.skillManager = skillManager;
        loadSlayerConfig();
    }

    @EventHandler
    public void onCombatXpGain(PlayerGainCombatXpEvent event) {
        Player player = event.getPlayer();
        ActiveSlayerQuest quest = getActiveSlayerQuest(player);
        if (quest == null || quest.getState() != ActiveSlayerQuest.QuestState.GATHERING_XP) {
            return;
        }

        String victimId = mobManager.getMobId(event.getVictim());
        if (victimId == null) return;
        CustomMobTemplate template = mobManager.getTemplate(victimId);
        if (template == null) return;

        String requiredCategory = getTargetCategoryForQuest(quest);
        if (requiredCategory != null && requiredCategory.equalsIgnoreCase(template.getMobCategory())) {
            quest.addXp(event.getXpAmount());
            plugin.getServer().getPluginManager().callEvent(new SlayerProgressUpdateEvent(player, quest));

            if (quest.isComplete()) {
                spawnSlayerBoss(player, quest);
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

    public void startQuest(Player player, String slayerType, int tier) {
        if (hasActiveQuest(player)) return;
        int xpToSpawn = slayerConfig.getInt(slayerType + ".tiers." + tier + ".xp-to-spawn");
        ActiveSlayerQuest quest = new ActiveSlayerQuest(slayerType, tier, xpToSpawn);
        activeQuests.put(player.getUniqueId(), quest);
        scoreboardManager.setActiveProvider(player, this);
    }

    public void endQuest(Player player) {
        activeQuests.remove(player.getUniqueId());
        scoreboardManager.clearActiveProvider(player);
        try {
            databaseManager.deleteActiveSlayerQuest(player.getUniqueId());
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

    public void spawnSlayerBoss(Player player, ActiveSlayerQuest quest) {
        String path = quest.getSlayerType() + ".tiers." + quest.getTier();
        String bossId = slayerConfig.getString(path + ".boss.id");
        if (bossId != null) {
            ConfigurationSection bossConfig = slayerConfig.getConfigurationSection(path + ".boss");
            new BossSpawnAnimation(plugin, player, this, player.getLocation(), bossId, bossConfig)
                    .runTaskTimer(plugin, 0L, 1L);
            player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>A powerful entity is being summoned..."));
        }
    }

    public void finalizeBossSpawn(Player player, ActiveSlayerQuest quest, String bossId, ConfigurationSection bossConfig, Location spawnLocation) {
        LivingEntity bossEntity = mobManager.spawnMob(bossId, spawnLocation, bossConfig);
        if (bossEntity != null) {
            quest.setActiveBoss(bossEntity);
            quest.setState(ActiveSlayerQuest.QuestState.BOSS_FIGHT);
            scoreboardManager.updateScoreboard(player);
            player.sendMessage("Slayer boss has spawned!");
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>The summoning failed!"));
            endQuest(player);
        }
    }

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
            case GATHERING_XP -> lines.add(String.format("<white>(<yellow>%,.0f</yellow>/<red>%,d</red>) <gray>Combat XP</gray>",
                    quest.getCurrentXp(), quest.getXpToSpawn()));
            case BOSS_FIGHT -> lines.add("<red><b>SLAY THE BOSS</b></red>");
            case AWAITING_CLAIM -> lines.add("<green><b>CLAIM REWARDS</b></green>");
        }
        return lines;
    }

    // This method was moved from MobManager
    public boolean isSlayerBoss(LivingEntity entity) {
        String mobId = mobManager.getMobId(entity);
        if (mobId == null) return false;
        return isSlayerBossId(mobId);
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

    // This method was moved from MobManager
    public List<String> getBossAbilities(LivingEntity entity) {
        String mobId = mobManager.getMobId(entity);
        if (mobId == null) return Collections.emptyList();
        return getBossAbilitiesById(mobId);
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
     * Finds all currently loaded LivingEntities that are identified as Slayer Bosses.
     * @return A list of active boss entities.
     */
    public List<LivingEntity> getActiveSlayerBosses() {
        List<LivingEntity> bosses = new ArrayList<>();
        // This is a simple implementation. For a large server, you might want to optimize this by tracking bosses on spawn.
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (isSlayerBoss(entity)) {
                    bosses.add(entity);
                }
            }
        }
        return bosses;
    }

    public void loadQuestForPlayer(Player player) throws SQLException {
        Optional<ActiveSlayerQuest> questOpt = databaseManager.loadActiveSlayerQuest(player.getUniqueId());
        questOpt.ifPresent(quest -> {
            activeQuests.put(player.getUniqueId(), quest);
            scoreboardManager.setActiveProvider(player, this);
        });
    }

    public void saveQuestForPlayer(Player player) throws SQLException {
        ActiveSlayerQuest quest = getActiveSlayerQuest(player);
        if (quest != null) {
            databaseManager.saveActiveSlayerQuest(player.getUniqueId(), quest);
        } else {
            databaseManager.deleteActiveSlayerQuest(player.getUniqueId());
        }
    }
}