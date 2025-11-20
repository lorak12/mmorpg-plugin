package org.nakii.mmorpg;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.commands.*;
import org.nakii.mmorpg.listeners.*;
import org.nakii.mmorpg.listeners.packet.CustomMiningPacketListener;
import org.nakii.mmorpg.managers.*;
import org.nakii.mmorpg.quest.QuestModule;
import org.nakii.mmorpg.tasks.BossAIController;
import org.nakii.mmorpg.tasks.ClimateTask;
import org.nakii.mmorpg.tasks.MobSpawningTask;
import org.nakii.mmorpg.tasks.PlayerMovementTracker;

import java.io.File;
import java.sql.SQLException;

public final class MMORPGCore extends JavaPlugin {

    private static MMORPGCore instance;

    // Managers
    private DatabaseManager databaseManager;
    private WorldTimeManager worldTimeManager;
    private RequirementManager requirementManager;
    private CooldownManager cooldownManager;
    private PlayerStateManager playerStateManager;
    private CombatTracker combatTracker;
    private DebuffManager debuffManager;
    private SlayerDataManager slayerDataManager;
    private ItemManager itemManager;
    private EnchantmentManager enchantmentManager;
    private ItemLoreGenerator itemLoreGenerator;
    private SkillManager skillManager;
    private RewardManager rewardManager;
    private CollectionManager collectionManager;
    private StatsManager statsManager;
    private DamageManager damageManager;
    private PlayerManager playerManager;
    private AbilityManager abilityManager;
    private MobManager mobManager;
    private RecipeManager recipeManager;
    private LootManager lootManager;
    private BossAbilityManager bossAbilityManager;
    private DoTManager doTManager;
    private TimedBuffManager timedBuffManager;
    private WorldManager worldManager;
    private RegenerationManager regenerationManager;
    private GUIManager guiManager;
    private HUDManager hudManager;
    private EconomyManager economyManager;
    private BankManager bankManager;
    private ScoreboardManager scoreboardManager;
    private TravelManager travelManager;
    private SlayerManager slayerManager;
    private EnchantmentEffectManager enchantmentEffectManager;
    private HealthManager healthManager;

    // Tasks
    private ClimateTask climateTask;
    private PlayerMovementTracker playerMovementTracker;

    private QuestModule questModule;
    private MiniMessage miniMessage;

    public static MMORPGCore getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Initializing MMORPGCore...");

        setupMiniMessage();
        setupDefaultItemFiles();

        try {
            // --- STEP 1: DATABASE & CORE UTILITIES (LOWEST/NO DEPENDENCIES) ---
            databaseManager = new DatabaseManager(this);
            databaseManager.connect(); // <<< FIX: This call now correctly resides inside the try block.

            cooldownManager = new CooldownManager();
            playerStateManager = new PlayerStateManager();
            combatTracker = new CombatTracker();
            debuffManager = new DebuffManager();
            slayerDataManager = new SlayerDataManager();
            doTManager = new DoTManager(this);
            itemManager = new ItemManager(this);
            enchantmentManager = new EnchantmentManager(this);
            worldManager = new WorldManager(this);
            worldTimeManager = new WorldTimeManager(this, databaseManager);
            worldTimeManager.loadTime(); // <<< FIX: This call also correctly resides inside the try block.
            economyManager = new EconomyManager(this, databaseManager);

            // --- STEP 2: MANAGERS DEPENDENT ON STEP 1 ---
            bankManager = new BankManager(this, economyManager, itemManager);
            regenerationManager = new RegenerationManager(this, worldManager);
            statsManager = new StatsManager(this, databaseManager, itemManager, enchantmentManager);
            playerManager = new PlayerManager(this, statsManager);
            healthManager = new HealthManager(this, statsManager, playerManager, combatTracker);
            hudManager = new HUDManager(this, statsManager, playerManager);
            mobManager = new MobManager(this, itemManager);
            damageManager = new DamageManager(this, statsManager, enchantmentManager);
            abilityManager = new AbilityManager(this, damageManager);

            // --- STEP 3: HIGH-LEVEL GAMEPLAY SYSTEMS (DEPEND ON PREVIOUS STEPS) ---
            timedBuffManager = new TimedBuffManager(this, statsManager);
            skillManager = new SkillManager(this, databaseManager, statsManager, mobManager, hudManager);
            rewardManager = new RewardManager(this, skillManager, itemManager, statsManager, economyManager);
            collectionManager = new CollectionManager(this, rewardManager, skillManager);
            requirementManager = new RequirementManager(this, skillManager, slayerDataManager, collectionManager);
            recipeManager = new RecipeManager(this, itemManager, requirementManager);
            enchantmentEffectManager = new EnchantmentEffectManager(doTManager, combatTracker, debuffManager, timedBuffManager, statsManager);
            itemLoreGenerator = new ItemLoreGenerator(this, enchantmentManager, itemManager, requirementManager);
            scoreboardManager = new ScoreboardManager(this, worldTimeManager, economyManager, worldManager);
            travelManager = new TravelManager(this, worldManager, requirementManager);
            lootManager = new LootManager(this, itemManager, statsManager, mobManager, itemLoreGenerator);
            slayerManager = new SlayerManager(this, databaseManager, slayerDataManager, mobManager, scoreboardManager, lootManager, collectionManager, skillManager);

            // --- STEP 4: RESOLVE CIRCULAR DEPENDENCIES WITH SETTER INJECTION ---
            statsManager.setSkillManager(skillManager);
            statsManager.setTimedBuffManager(timedBuffManager);
            statsManager.setEnchantmentEffectManager(enchantmentEffectManager);
            skillManager.setRewardManager(rewardManager);

            // --- STEP 5: LISTENERS, TASKS, COMMANDS, MODULES ---
            worldManager.loadWorlds();
            guiManager = new GUIManager(this, skillManager);

            getLogger().info("All managers initialized successfully.");

            registerListeners();
            getLogger().info("Listeners registered.");

            registerCommands();
            getLogger().info("Commands registered.");

            startTasks();
            getLogger().info("Tasks started.");

            initializeQuestModule();

        } catch (SQLException e) { // The catch block is now valid and will handle database connection errors.
            getLogger().severe("Could not connect to the database! Disabling plugin...");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("MMORPGCore enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (questModule != null) {
            questModule.disable();
        }
        if (worldTimeManager != null) {
            worldTimeManager.saveTime();
        }
        // Graceful shutdown for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                if (skillManager != null) skillManager.savePlayerData(player);
                if (economyManager != null) economyManager.unloadPlayer(player);
                if (slayerManager != null) slayerManager.saveQuestForPlayer(player);
                // Add other save-on-quit logic here
            } catch (SQLException e) {
                getLogger().severe("Failed to save data for " + player.getName() + " on shutdown.");
                e.printStackTrace();
            }
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("MMORPGCore disabled.");
    }

    private void setupMiniMessage() {
        this.miniMessage = MiniMessage.builder().postProcessor(component -> component.style(component.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))).build();
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        // Listeners are now created with their dependencies injected
        pm.registerEvents(new AbilityListener(this, itemManager, abilityManager, playerManager, cooldownManager), this);
        pm.registerEvents(new AlchemyListener(skillManager), this);
        pm.registerEvents(new AnvilListener(this, enchantmentManager), this);
        BlockBreakListener blockBreakListener = new BlockBreakListener(this, worldManager, regenerationManager, collectionManager, itemManager, skillManager);
        pm.registerEvents(blockBreakListener, this);
        pm.registerEvents(new BlockPlaceListener(worldManager), this);
        pm.registerEvents(new CarpentryListener(skillManager), this);
        pm.registerEvents(new CraftingTableListener(this, recipeManager, itemManager, itemLoreGenerator), this);
        pm.registerEvents(new EnchantingTableListener(this, enchantmentManager, skillManager), this);
        pm.registerEvents(new EntitySpawningListener(this, mobManager), this);
        pm.registerEvents(new FarmingListener(skillManager), this);
        pm.registerEvents(new FishingListener(skillManager), this);
        pm.registerEvents(new ForagingListener(skillManager, worldManager), this);
        pm.registerEvents(new GenericDamageListener(this, hudManager, mobManager, playerManager, combatTracker), this);
        pm.registerEvents(new GUIListener(this), this);
        pm.registerEvents(new InventoryListener(this, statsManager), this);
        pm.registerEvents(new PlayerConnectionListener(this, databaseManager, skillManager, slayerDataManager, collectionManager, statsManager, playerManager, economyManager, slayerManager, scoreboardManager, playerMovementTracker), this); // Movement tracker is a task
        pm.registerEvents(new PlayerDamageListener(this, combatTracker, enchantmentManager, enchantmentEffectManager, statsManager, damageManager, mobManager, slayerManager, slayerDataManager, scoreboardManager, lootManager, collectionManager, skillManager, playerManager, hudManager), this);
        pm.registerEvents(new PlayerDeathListener(this, economyManager, playerManager, statsManager), this);
        pm.registerEvents(new PristineItemListener(), this);
        pm.registerEvents(new ProjectileListener(this, enchantmentManager), this);
        pm.registerEvents(new RequirementListener(requirementManager), this);
        pm.registerEvents(new ScoreboardListener(scoreboardManager), this);
        pm.registerEvents(new SlayerProgressListener(this, slayerManager, mobManager), this);

        // Register BankManager and SlayerManager as they are also listeners
        pm.registerEvents(bankManager, this);
        pm.registerEvents(slayerManager, this);

        // ProtocolLib
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new CustomMiningPacketListener(this, blockBreakListener, worldManager, statsManager));
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

            saveResource("items/items.yml", false);

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
        getCommand("mmorpg").setExecutor(new MmorpgCommand(this, itemManager, mobManager, recipeManager, worldManager));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("skills").setExecutor(new SkillsCommand(guiManager));
        getCommand("giveitem").setExecutor(new GiveItemCommand(this, itemManager, itemLoreGenerator));
        getCommand("spawnmob").setExecutor(new SpawnMobCommand(this, mobManager));
        getCommand("customenchant").setExecutor(new EnchantCommand(this, enchantmentManager));
        getCommand("slayer").setExecutor(new SlayerCommand(this, slayerManager, slayerDataManager, economyManager, requirementManager));
        getCommand("craft").setExecutor(new CraftCommand(this, recipeManager, itemManager, itemLoreGenerator));
        getCommand("eco").setExecutor(new EcoCommand(economyManager));
        getCommand("bank").setExecutor(new BankCommand(this, worldTimeManager, economyManager, bankManager));
        getCommand("debugclimate").setExecutor(new ClimateDebugCommand(climateTask));
        getCommand("collections").setExecutor(new CollectionCommand(this, collectionManager));
        getCommand("worldadmin").setExecutor(new WorldAdminCommand(this, worldManager));
        getCommand("mmorpgdebug").setExecutor(new MmorpgDebugCommand(this, climateTask, worldManager));
        getCommand("travel").setExecutor(new TravelCommand(travelManager, worldManager));
    }

    private void startTasks() {
        worldTimeManager.startTask();
        healthManager.startHealthRegenTask();
        if (hudManager != null) hudManager.startHUDTask();

        new MobSpawningTask(this, worldManager, mobManager).runTaskTimer(this, 20L * 10, 20L * 5);
        climateTask = new ClimateTask(this, worldManager, playerStateManager, statsManager);
        climateTask.runTaskTimer(this, 20L * 5, 20L);
        playerMovementTracker = new PlayerMovementTracker(worldManager, requirementManager);
        playerMovementTracker.runTaskTimer(this, 100L, 20L);
        new BossAIController(mobManager, bossAbilityManager, slayerManager).runTaskTimer(this, 200L, 20L);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (playerManager != null) playerManager.regenerateMana();
            }
        }.runTaskTimer(this, 20L, 20L);
        startAutoSaveTask();
    }

    private void initializeQuestModule() {

        try {
            getLogger().info("Initializing Quest Module (Delayed)...");


            questModule = new QuestModule(this);
            questModule.enable();
            getLogger().info("Quest Module initialized successfully.");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize the Quest Module!");
            e.printStackTrace();
        }


    }

    private void startAutoSaveTask() {
        long interval = 20L * 60 * 5; // Every 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                getLogger().info("Auto-saving player skill data...");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    skillManager.savePlayerData(player);
                }
                getLogger().info("Auto-save complete.");
            }
        }.runTaskTimer(this, interval, interval);
    }

    public File getPluginFile() {
        return getFile();
    }


    public MiniMessage getMiniMessage() {
        return this.miniMessage;
    }


    public QuestModule getQuestModule() {
        return questModule;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public ItemLoreGenerator getItemLoreGenerator() {
        return itemLoreGenerator;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

}