package org.nakii.mmorpg;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.commands.*;
import org.nakii.mmorpg.listeners.*;
import org.nakii.mmorpg.listeners.packet.CustomMiningPacketListener;
import org.nakii.mmorpg.managers.*;
import org.nakii.mmorpg.quest.QuestManager;
import org.nakii.mmorpg.quest.conversation.ConversationManager;
import org.nakii.mmorpg.quest.hologram.HologramManager;
import org.nakii.mmorpg.tasks.*;

import java.io.File;
import java.sql.SQLException;

public final class MMORPGCore extends JavaPlugin {

    private static MMORPGCore instance;

    // All manager instances
    private ItemManager itemManager;
    private StatsManager statsManager;
    private HealthManager healthManager;
    private DamageManager damageManager;
    private DatabaseManager databaseManager;
    private SkillManager skillManager;
    private GUIManager guiManager;
    private MobManager mobManager;
    private ZoneManager zoneManager;
    private LootManager lootManager;
    private RecipeManager recipeManager;
    private HUDManager hudManager;
    private EnchantmentManager enchantmentManager;
    private EnchantmentEffectManager enchantmentEffectManager;
    private CombatTracker combatTracker;
    private DebuffManager debuffManager;
    private DoTManager doTManager;
    private TimedBuffManager timedBuffManager;
    private PlayerStateManager playerStateManager;
    private ItemLoreGenerator itemLoreGenerator;
    private SlayerManager slayerManager;
    private EconomyManager economyManager;
    private WorldTimeManager worldTimeManager;
    private ScoreboardManager scoreboardManager;
    private BankManager bankManager;
    private RegenerationManager regenerationManager;
    private SlayerDataManager slayerDataManager;
    private BossAbilityManager bossAbilityManager;
    private CollectionManager collectionManager;
    private RewardManager rewardManager;
    private WorldManager worldManager;
    private RequirementManager requirementManager;
    private TravelManager travelManager;
    private PlayerManager playerManager;
    private AbilityManager abilityManager;
    private CooldownManager cooldownManager;


    private BlockBreakListener blockBreakListener;

    private ClimateTask climateTask;
    private MobSpawningTask mobSpawningTask;
    private PlayerMovementTracker playerMovementTracker;
    private QuestManager questManager;
    private ConversationManager conversationManager;
    private HologramManager hologramManager;

    private MiniMessage miniMessage;



    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Initializing MMORPGCore...");

        // This builder configures a post-processor, which is a function that runs
        // on every component after it has been parsed by MiniMessage.
        // We use it to take the component's existing style and explicitly set
        // the ITALIC decoration to FALSE, preserving all other formatting.
        this.miniMessage = MiniMessage.builder()
                .postProcessor(component -> component.style(
                        component.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                )
                .build();

        setupDefaultItemFiles();

        try {



            // --- STEP 1: INITIALIZE ALL MANAGERS FIRST ---
            // This is the critical fix. All manager instances must be created before
            // any listeners that might use them are registered.
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();

            worldTimeManager = new WorldTimeManager(this);
            worldTimeManager.loadTime();

            // Core Systems
            statsManager = new StatsManager(this);
            skillManager = new SkillManager(this);
            damageManager = new DamageManager(this);
            playerStateManager = new PlayerStateManager();
            rewardManager = new RewardManager(this);
            requirementManager = new RequirementManager(this);
            playerManager = new PlayerManager(this);
            abilityManager = new AbilityManager(this);
            cooldownManager = new CooldownManager();

            conversationManager = new ConversationManager(this);
            questManager = new QuestManager(this);
            hologramManager = new HologramManager(this);

            // Item & Content Systems
            itemManager = new ItemManager(this);
            itemLoreGenerator = new ItemLoreGenerator(this);
            mobManager = new MobManager(this);
            recipeManager = new RecipeManager(this);
            lootManager = new LootManager(this);
            slayerDataManager = new SlayerDataManager();
            slayerManager = new SlayerManager(this);
            bossAbilityManager = new BossAbilityManager(this);
            collectionManager = new CollectionManager(this);


            // Enchantment Systems
            enchantmentManager = new EnchantmentManager(this);
            enchantmentEffectManager = new EnchantmentEffectManager(this);

            // Combat & Environment Systems
            combatTracker = new CombatTracker(); // This was the source of the NullPointerException
            debuffManager = new DebuffManager();
            doTManager = new DoTManager(this);
            timedBuffManager = new TimedBuffManager();

            // World & UI Systems
            worldManager = new WorldManager(this);
            zoneManager = new ZoneManager(this);
            regenerationManager = new RegenerationManager(this);
            guiManager = new GUIManager(this);
            hudManager = new HUDManager(this);
            economyManager = new EconomyManager(this);
            bankManager = new BankManager(this);
            scoreboardManager = new ScoreboardManager(this);
            travelManager = new TravelManager(this);

            // Load world managed by WorldManager
            worldManager.loadWorlds();

            // Standalone manager for health regen task
            healthManager = new HealthManager(this);

            getLogger().info("All managers initialized successfully.");

            mobSpawningTask = new MobSpawningTask(this);
            mobSpawningTask.runTaskTimer(this, 20L * 10, 20L * 5); // 10s delay, 5s interval

            climateTask = new ClimateTask(this);
            climateTask.runTaskTimer(this, 20L * 5, 20L); // 5s delay, 1s interval for responsiveness

            playerMovementTracker = new PlayerMovementTracker(this);
            playerMovementTracker.runTaskTimer(this, 100L, 20L); // Start after 5s, check every 1s

            new BossAIController(this).runTaskTimer(this, 200L, 20L); // Start after 10s, run every 1s

            new BukkitRunnable() {
                @Override
                public void run() {
                    playerManager.regenerateMana();
                }
            }.runTaskTimer(this, 20L, 20L);


            // --- STEP 2: REGISTER LISTENERS ---
            // Now that all managers are guaranteed to be non-null, we can safely register listeners.
            registerListeners();
            getLogger().info("Listeners registered.");

            // --- STEP 3: REGISTER COMMANDS ---
            registerCommands();
            getLogger().info("Commands registered.");

            // --- STEP 4: START TASKS ---
            worldTimeManager.startTask();
            healthManager.startHealthRegenTask();
            hudManager.startHUDTask();



            startAutoSaveTask();
            getLogger().info("Tasks started.");

        } catch (SQLException e) {
            getLogger().severe("Could not connect to the database! Disabling plugin...");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return; // Stop execution if DB connection fails
        }

        getLogger().info("MMORPGCore enabled successfully.");
    }



    @Override
    public void onDisable() {

        // Save the time when the server shuts down
        if (worldTimeManager != null) {
            worldTimeManager.saveTime();
        }

        if (skillManager != null && databaseManager != null) {
            getLogger().info("Saving data for all online players before shutdown...");
            for (Player player : Bukkit.getOnlinePlayers()) {
                skillManager.savePlayerData(player);
                getLogger().info(" - Saved data for " + player.getName());
            }
            getLogger().info("Player data saving complete.");
        }

        if (this.playerMovementTracker != null) {
            this.playerMovementTracker.cancel();
        }

        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        if (questManager != null && databaseManager != null) {
            getLogger().info("Saving quest data for all online players...");
            for (Player player : Bukkit.getOnlinePlayers()) {
                questManager.unloadPlayerData(player);
                getLogger().info(" - Saved quest data for " + player.getName());
            }
        }

        getLogger().info("MMORPGCore disabled.");
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();

        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            pm.registerEvents(new NPCInteractionListener(this), this);
            pm.registerEvents(new ConversationInputListener(this), this);
        } else {
            getLogger().warning("Citizens plugin not found. Quest system will not be fully functional.");
        }

        pm.registerEvents(new PristineItemListener(), this);

        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new EntitySpawningListener(this), this);
        pm.registerEvents(new FarmingListener(this), this);
        pm.registerEvents(new CarpentryListener(this), this);
        pm.registerEvents(new AlchemyListener(this), this);
        pm.registerEvents(new FishingListener(this), this);
        pm.registerEvents(new ForagingListener(this), this);
        pm.registerEvents(new GUIListener(this), this);
        pm.registerEvents(new EnchantingTableListener(this), this);
        pm.registerEvents(new AnvilListener(this), this);
        pm.registerEvents(new PlayerDamageListener(this), this);
        pm.registerEvents(new ProjectileListener(this), this);
        pm.registerEvents(new PlayerConnectionListener(this), this);
        pm.registerEvents(new GenericDamageListener(this), this);
        pm.registerEvents(new RequirementListener(this), this);
        pm.registerEvents(new SlayerProgressListener(this), this);
        pm.registerEvents(new CraftingTableListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new ScoreboardListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);
        pm.registerEvents(new AbilityListener(this), this);
        pm.registerEvents(new ObjectiveProgressListener(this), this);



        this.blockBreakListener = new BlockBreakListener(this);
        pm.registerEvents(this.blockBreakListener, this);

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        CustomMiningPacketListener packetListener = new CustomMiningPacketListener(this);
        protocolManager.addPacketListener(packetListener);

    }


    private void setupDefaultItemFiles() {
        File itemsFolder = new File(getDataFolder(), "items");
        if (!itemsFolder.exists()) {
            getLogger().info("First launch detected: Creating default item files...");

            saveResource("items/armor/final_destination.yml", false);
            saveResource("items/armor/starter_gear.yml", false);

            saveResource("items/materials/mob_drops.yml", false);
            saveResource("items/materials/refined.yml", false);

            saveResource("items/special/event_items.yml", false);

            saveResource("items/tools/pickaxes.yml", false);

            saveResource("items/weapons/swords.yml", false);
            saveResource("items/weapons/bows.yml", false);
            saveResource("items/weapons/daggers.yml", false);

            saveResource("items/items.yml",false);

            getLogger().info("Default item files created successfully.");
        }

        // Check for the 'mobs' folder
        File mobsFolder = new File(getDataFolder(), "mobs");
        if (!mobsFolder.exists()) {
            getLogger().info("Creating default mob configuration files...");

            saveResource("mobs/undead/crypt_ghoul.yml", false);
            saveResource("mobs/slayer_bosses.yml", false);
            saveResource("mobs/obisidan_sanctuary.yml", false);
            saveResource("mobs/zombies.yml", false);

            getLogger().info("Default mob files created successfully.");
        }

        File recipeFolder = new File(getDataFolder(), "recipes");
        if (!recipeFolder.exists()) {
            getLogger().info("Creating default recipes configuration files...");

            saveResource("recipes/custom_recipes.yml", false);

            getLogger().info("Default recipes files created successfully.");
        }

        File worldsFolder = new File(getDataFolder(), "worlds");
        if (!worldsFolder.exists()) {
            getLogger().info("Creating default world configuration files...");

            saveResource("worlds/the_mines/world.yml", false);
            saveResource("worlds/the_mines/zones.yml", false);
            saveResource("worlds/starting_island/world.yml", false);
            saveResource("worlds/starting_island/zones.yml", false);

            getLogger().info("Default worlds created successfully.");
        }

        // Save single config files
        saveResource("skills.yml", false);
        saveResource("slayers.yml", false);
//        saveResource("defaults.yml", false);
    }

    private void registerCommands() {
        // The command constructor now needs the listener instance
        getCommand("mmorpg").setExecutor(new MmorpgCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("skills").setExecutor(new SkillsCommand(this));
        getCommand("giveitem").setExecutor(new GiveItemCommand(this));
        getCommand("spawnmob").setExecutor(new SpawnMobCommand(this));
        getCommand("customenchant").setExecutor(new EnchantCommand(this));
        getCommand("openhex").setExecutor(new HexCommand(this));
        getCommand("slayer").setExecutor(new SlayerCommand(this));
        getCommand("craft").setExecutor(new CraftCommand(this));
        getCommand("eco").setExecutor(new EcoCommand(this));
        getCommand("bank").setExecutor(new BankCommand(this));
        getCommand("debugclimate").setExecutor(new ClimateDebugCommand(climateTask));
        getCommand("collections").setExecutor(new CollectionCommand(this));
        getCommand("worldadmin").setExecutor(new WorldAdminCommand(this));
        getCommand("mmorpgdebug").setExecutor(new MmorpgDebugCommand(this));
        getCommand("travel").setExecutor(new TravelCommand(this));
        getCommand("questadmin").setExecutor(new QuestAdminCommand());
    }
    private void startAutoSaveTask() {
        long interval = 20L * 60 * 5; // Every 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                getLogger().info("Auto-saving player skill data...");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    getSkillManager().savePlayerData(player);
                }
                getLogger().info("Auto-save complete.");
            }
        }.runTaskTimer(this, interval, interval);
    }

    public static MMORPGCore getInstance() { return instance; }

    public MiniMessage getMiniMessage() {
        return this.miniMessage;
    }

    public ItemManager getItemManager() { return itemManager; }
    public ItemLoreGenerator getItemLoreGenerator() { return itemLoreGenerator; }
    public StatsManager getStatsManager() { return statsManager; }
    public HealthManager getHealthManager() { return healthManager; }
    public DamageManager getDamageManager() { return damageManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
    public MobManager getMobManager() { return mobManager; }
    public ZoneManager getZoneManager() { return zoneManager; }
    public LootManager getLootManager() { return lootManager;  }
    public HUDManager getHUDManager() { return hudManager; }
    public EnchantmentManager getEnchantmentManager() { return enchantmentManager; }
    public EnchantmentEffectManager getEnchantmentEffectManager() { return enchantmentEffectManager; }
    public CombatTracker getCombatTracker() { return combatTracker; }
    public DebuffManager getDebuffManager() { return debuffManager; }
    public DoTManager getDoTManager() { return doTManager; }
    public TimedBuffManager getTimedBuffManager() { return timedBuffManager; }
    public PlayerStateManager getPlayerStateManager() { return playerStateManager; }
    public SlayerManager getSlayerManager() { return slayerManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public WorldTimeManager getWorldTimeManager() { return worldTimeManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public BankManager getBankManager() { return bankManager; }
    public RegenerationManager getRegenerationManager() { return regenerationManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public AbilityManager getAbilityManager() { return abilityManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public ClimateTask getClimateTask() {
        return climateTask;
    }
    public SlayerDataManager getSlayerDataManager() {
        return slayerDataManager;
    }
    public BossAbilityManager getBossAbilityManager() {
        return bossAbilityManager;
    }
    public CollectionManager getCollectionManager() {
        return collectionManager;
    }
    public BlockBreakListener getBlockBreakListener() {
        return this.blockBreakListener;
    }
    public RewardManager getRewardManager() {
        return rewardManager;
    }
    public WorldManager getWorldManager() { return worldManager; }
    public RequirementManager getRequirementManager() { return requirementManager; }
    public PlayerMovementTracker getPlayerMovementTracker() { return playerMovementTracker; }
    public TravelManager getTravelManager() { return travelManager; }
    public QuestManager getQuestManager() { return questManager; }
    public ConversationManager getConversationManager() { return conversationManager; }
    public HologramManager getHologramManager() { return hologramManager; }

}