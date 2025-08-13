package org.nakii.mmorpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.nakii.mmorpg.commands.*;
import org.nakii.mmorpg.listeners.*;
import org.nakii.mmorpg.managers.*;
import java.io.File;
import java.sql.SQLException;


public final class MMORPGCore extends JavaPlugin {

    private static MMORPGCore instance;

    // Manager Instances
    private ItemManager itemManager;
    private StatsManager statsManager;
    private HealthManager healthManager;
    private DamageManager damageManager;
    private DatabaseManager databaseManager;
    private SkillManager skillManager;
    private GUIManager guiManager;
    private MobManager mobManager;
    private ZoneManager zoneManager;
    private AbilityManager abilityManager;
    private LootManager lootManager;
    private RecipeManager recipeManager;
    private EnvironmentManager environmentManager;
    private HUDManager hudManager;
    private ZoneMobSpawnerManager zoneMobSpawner;
    private ZoneWandListener zoneWandListener;
    private EnchantmentManager enchantmentManager;

    // API Hooks
    private boolean libsDisguisesEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // Hook into APIs first
        setupAPIHooks();

        startAutoSaveTask();

        // Create config folders + files
        saveDefaultConfig();
        createItemsFolder();
        saveResource("mobs.yml", false);
        saveResource("zones.yml", false);

        try {
            // Initialize managers
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();


            skillManager = new SkillManager(this);
            itemManager = new ItemManager(this);
            statsManager = new StatsManager(this);
            healthManager = new HealthManager(this);
            damageManager = new DamageManager(this);
            guiManager = new GUIManager(this);
            mobManager = new MobManager(this);
            recipeManager = new RecipeManager(this);
            abilityManager = new AbilityManager(this);
            lootManager = new LootManager(this);
            environmentManager = new EnvironmentManager(this);
            hudManager = new HUDManager(this);
            zoneManager = new ZoneManager(this);
            zoneMobSpawner = new ZoneMobSpawnerManager(this);
            enchantmentManager = new EnchantmentManager(this);

            // Register events + commands
            registerListeners();
            registerCommands();

            healthManager.startHealthRegenTask();
            environmentManager.startEnvironmentTask();
            zoneMobSpawner.startSpawnTask();

            hudManager.startHUDTask();
            startAutoSaveTask();

        } catch (SQLException e) {
            getLogger().severe("Could not connect to the database! Disabling plugin...");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        getLogger().info("MMORPGCore enabled successfully.");
    }

    @Override
    public void onDisable() {
        // Save all online player data FIRST
        if (skillManager != null && databaseManager != null) {
            getLogger().info("Saving data for all online players before shutdown...");
            for (Player player : Bukkit.getOnlinePlayers()) {
                skillManager.savePlayerData(player);
                getLogger().info(" - Saved data for " + player.getName());
            }
            getLogger().info("Player data saving complete.");
        }

        // Now, it's safe to disconnect from the database
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        getLogger().info("MMORPGCore disabled.");
    }


    private void setupAPIHooks() {

        // --- LIBSDISGUISES ---
        // This check should remain, because we are NOT shading LibsDisguises.
        Plugin disguises = Bukkit.getPluginManager().getPlugin("LibsDisguises");
        if (disguises != null && disguises.isEnabled()) {
            libsDisguisesEnabled = true;
            getLogger().info("Hooked into LibsDisguises.");
        } else {
            getLogger().warning("LibsDisguises not found. Mob disguises disabled.");
        }
    }

    private void createItemsFolder() {
        File itemsFolder = new File(getDataFolder(), "items");
        if (!itemsFolder.exists() && itemsFolder.mkdirs()) {
            saveResource("items/example_sword.yml", false);
        }
        saveResource("mobs.yml", false);
        saveResource("zones.yml", false);
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();

        // 1. Create the listener instance and assign it to our class field.
        this.zoneWandListener = new ZoneWandListener(this);
        // 2. Register that specific, stored instance.
        pm.registerEvents(this.zoneWandListener, this);

        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new CombatListener(this), this);
        pm.registerEvents(new EntitySpawningListener(this), this);
        pm.registerEvents(new MiningListener(this), this);
        pm.registerEvents(new FarmingListener(this), this);
        pm.registerEvents(new CarpentryListener(this), this);
        pm.registerEvents(new AlchemyListener(this), this);
        pm.registerEvents(new FishingListener(this), this);
        pm.registerEvents(new ForagingListener(this), this);
        pm.registerEvents(new GUIListener(this), this);
        pm.registerEvents(new RecipeListener(this), this);
        pm.registerEvents(new EnchantingTableListener(this), this);
        pm.registerEvents(new AnvilListener(this), this);
    }

    private void registerCommands() {
        // The command constructor now needs the listener instance
        getCommand("mmorpg").setExecutor(new MmorpgCommand(this, this.zoneWandListener));
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("skills").setExecutor(new SkillsCommand(this));
        getCommand("giveitem").setExecutor(new GiveItemCommand(this));
        getCommand("spawnmob").setExecutor(new SpawnMobCommand(this));
        getCommand("customenchant").setExecutor(new EnchantCommand(this));
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

    // Static access to plugin
    public static MMORPGCore getInstance() {
        return instance;
    }

    // API + Manager Getters
    public boolean isLibsDisguisesEnabled() { return libsDisguisesEnabled; }

    public ItemManager getItemManager() { return itemManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public HealthManager getHealthManager() { return healthManager; }
    public DamageManager getDamageManager() { return damageManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public SkillManager getSkillManager() { return skillManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
    public MobManager getMobManager() { return mobManager; }
    public ZoneManager getZoneManager() { return zoneManager; }
    public AbilityManager getAbilityManager() { return abilityManager; }
    public LootManager getLootManager() { return lootManager;  }
    public EnvironmentManager getEnvironmentManager() { return environmentManager; }
    public HUDManager getHudManager() { return hudManager; }
    public EnchantmentManager getEnchantmentManager() { return enchantmentManager; }
}
